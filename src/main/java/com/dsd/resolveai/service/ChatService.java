package com.dsd.resolveai.service;

import com.dsd.resolveai.advisor.PIIRedactionAdvisor;
import com.dsd.resolveai.tools.DatabaseTools;
import com.dsd.resolveai.tools.IncidentTools;
import com.dsd.resolveai.tools.RunbookTools;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore,
            IncidentTools incidentTools,
            RunbookTools runbookTools,
            DatabaseTools databaseTools,
            @Value("classpath:system-prompt.st") Resource systemPromptResource) {

        SystemPromptTemplate systemPromptTemplate =
                new SystemPromptTemplate(systemPromptResource);
        String systemPrompt = systemPromptTemplate.render();

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    new PIIRedactionAdvisor()
                )
                .defaultTools(incidentTools, runbookTools, databaseTools)
                .build();

        this.vectorStore = vectorStore;
    }

    public String chat(String conversationId, String message) {
        return chatClient
                .prompt(message)
                .advisors(VectorStoreChatMemoryAdvisor.builder(vectorStore)
                        .conversationId(conversationId)
                        .build())
                .call()
                .content();
    }
}
