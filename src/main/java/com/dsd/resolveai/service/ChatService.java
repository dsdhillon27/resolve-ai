package com.dsd.resolveai.service;

import com.dsd.resolveai.enums.RouteDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    private final ChatClient routerClient;
    private final ChatClient sreClient;
    private final ChatClient generalClient;
    private final VectorStore vectorStore;

    public ChatService(
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("sreClient") ChatClient sreClient,
            @Qualifier("generalClient") ChatClient generalClient,
            VectorStore vectorStore) {

        this.routerClient = routerClient;
        this.sreClient = sreClient;
        this.generalClient = generalClient;
        this.vectorStore = vectorStore;
    }

    public String chat(String conversationId, String message) {

        ChatClient activeClient = generalClient;

        try {
            RouteDecision routeDecision = routerClient.prompt()
                    .call()
                    .entity(RouteDecision.class);

            if (routeDecision != null && routeDecision.route() != null) {
                log.info("Route reasoning: {}", routeDecision.reasoning());
                log.info("Route agent: {}", routeDecision.route());

                activeClient = switch (routeDecision.route()) {
                    case SRE_AGENT -> sreClient;
                    case GENERAL_QA -> generalClient;
                };
            } else {
                log.warn("Router returned null or invalid route, falling back to GENERAL_QA");
            }
        } catch (Exception e) {
            log.warn("Router Agent failed to classify intent, falling back to GENERAL_QA", e);
        }

        return activeClient
                .prompt(message)
                .advisors(VectorStoreChatMemoryAdvisor.builder(vectorStore)
                        .conversationId(conversationId)
                        .build())
                .call()
                .content();
    }
}
