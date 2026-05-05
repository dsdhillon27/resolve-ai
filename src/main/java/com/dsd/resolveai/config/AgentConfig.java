package com.dsd.resolveai.config;

import com.dsd.resolveai.advisor.PIIRedactionAdvisor;
import com.dsd.resolveai.tools.DatabaseTools;
import com.dsd.resolveai.tools.IncidentTools;
import com.dsd.resolveai.tools.RunbookTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AgentConfig {

    @Bean
    @Qualifier("routerClient")
    public ChatClient routeClient(
            ChatModel chatModel,
            @Value("classpath:prompt/router-system-prompt.st") Resource resource) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(resource);
        String routerSystemPrompt = systemPromptTemplate.render();

        return ChatClient.builder(chatModel)
                .defaultSystem(routerSystemPrompt)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    @Qualifier("sreClient")
    public ChatClient sreClient(
            ChatModel chatModel,
            @Value("classpath:prompt/sre-system-prompt.st") Resource resource,
            IncidentTools incidentTools,
            RunbookTools runbookTools,
            DatabaseTools databaseTools) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(resource);
        String sreSystemPrompt = systemPromptTemplate.render();

        return ChatClient.builder(chatModel)
                .defaultSystem(sreSystemPrompt)
                .defaultAdvisors(new SimpleLoggerAdvisor(), new PIIRedactionAdvisor())
                .defaultTools(incidentTools, runbookTools, databaseTools)
                .build();
    }

    @Bean
    @Qualifier("generalClient")
    public ChatClient generalClient(
            ChatModel chatModel,
            @Value("classpath:prompt/general-system-prompt.st") Resource resource
    ){
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(resource);
        String generalSystemPrompt = systemPromptTemplate.render();

        return ChatClient.builder(chatModel)
                .defaultSystem(generalSystemPrompt)
                .defaultAdvisors(new SimpleLoggerAdvisor(), new PIIRedactionAdvisor())
                .build();
    }

}
