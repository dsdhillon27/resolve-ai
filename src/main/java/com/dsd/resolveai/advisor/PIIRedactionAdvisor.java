package com.dsd.resolveai.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

public class PIIRedactionAdvisor implements CallAdvisor {

    @Override
    public String getName() {
        return "PIIRedactionAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        List<Message> originalMessages = request.prompt().getInstructions();
        List<Message> redactedMessages = new ArrayList<>();

        for (Message message : originalMessages) {
            if (message instanceof UserMessage) {
                String originalText = message.getText();

                String redactedText = originalText.replaceAll("([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})", "[REDACTED_EMAIL]");
                redactedMessages.add(new UserMessage(redactedText));
            } else {
                redactedMessages.add(message);
            }
        }

        Prompt safePrompt = new Prompt(redactedMessages, request.prompt().getOptions());

        ChatClientRequest safeRequest = request.mutate()
                .prompt(safePrompt)
                .build();

        return chain.nextCall(safeRequest);
    }
}



