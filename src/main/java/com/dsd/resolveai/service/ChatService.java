package com.dsd.resolveai.service;

import com.dsd.resolveai.enums.RouteDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {

    /** Intent classification needs recency, not depth; the router runs on every request. */
    private static final int ROUTER_HISTORY_SIZE = 6;

    /** Keeps a long agent reply from dominating the router prompt. */
    private static final int ROUTER_MESSAGE_MAX_CHARS = 300;

    private final ChatClient routerClient;
    private final ChatClient sreClient;
    private final ChatClient generalClient;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    public ChatService(
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("sreClient") ChatClient sreClient,
            @Qualifier("generalClient") ChatClient generalClient,
            VectorStore vectorStore,
            ChatMemory chatMemory) {

        this.routerClient = routerClient;
        this.sreClient = sreClient;
        this.generalClient = generalClient;
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    public String chat(String conversationId, String message) {

        ChatClient activeClient = route(conversationId, message);

        return activeClient
                .prompt(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(conversationId)
                        .build())
                .call()
                .content();
    }

    private ChatClient route(String conversationId, String message) {

        try {
            RouteDecision routeDecision = routerClient.prompt()
                    .user(buildRouterInput(conversationId, message))
                    .call()
                    .entity(RouteDecision.class);

            if (routeDecision != null && routeDecision.route() != null) {
                log.info("Route reasoning: {}", routeDecision.reasoning());
                log.info("Route agent: {}", routeDecision.route());

                return switch (routeDecision.route()) {
                    case SRE_AGENT -> sreClient;
                    case GENERAL_QA -> generalClient;
                };
            }
            log.warn("Router returned null or invalid route, falling back to GENERAL_QA");
        } catch (Exception e) {
            log.warn("Router Agent failed to classify intent, falling back to GENERAL_QA", e);
        }

        return generalClient;
    }

    private String buildRouterInput(String conversationId, String message) {

        List<Message> history = chatMemory.get(conversationId);

        if (history.isEmpty()) {
            return "LATEST USER MESSAGE:\n" + message;
        }

        String transcript = history.stream()
                .skip(Math.max(0, history.size() - ROUTER_HISTORY_SIZE))
                .map(this::renderForRouter)
                .collect(Collectors.joining("\n"));

        return """
                CONVERSATION SO FAR:
                %s

                LATEST USER MESSAGE:
                %s""".formatted(transcript, message);
    }

    private String renderForRouter(Message message) {
        String text = message.getText() == null ? "" : message.getText();
        if (text.length() > ROUTER_MESSAGE_MAX_CHARS) {
            text = text.substring(0, ROUTER_MESSAGE_MAX_CHARS) + "…";
        }
        return message.getMessageType().name() + ": " + text;
    }
}
