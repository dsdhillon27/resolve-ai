package com.dsd.resolveai.service;

import com.dsd.resolveai.advisor.PIIRedactionAdvisor;
import com.dsd.resolveai.tools.IncidentTools;
import jakarta.persistence.EntityManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(10)
            .build();

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore,
            IncidentTools incidentTools) {

        String systemPrompt = """
            You are an enterprise AI agent.  
            Please answer only as per the context provided.
            """;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    QuestionAnswerAdvisor.builder(vectorStore).build(),
                    MessageChatMemoryAdvisor.builder(chatMemory)
                            .conversationId("default-session")
                            .build(),
                    new PIIRedactionAdvisor()
                )
                .defaultTools(incidentTools)
                .build();
    }

    public String chat(String message) {
        return chatClient
                .prompt(message)
                .call()
                .content();
    }
}
