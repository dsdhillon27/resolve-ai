package com.dsd.resolveai.controller;

import com.dsd.resolveai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{conversationId}")
    public String chat(@PathVariable("conversationId") String conversationId,
                       @RequestParam String message) {
        return chatService.chat(conversationId, message);
    }
}
