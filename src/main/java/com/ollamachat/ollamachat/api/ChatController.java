package com.ollamachat.ollamachat.api;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ollamachat.ollamachat.Servies.OllamaService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    private final OllamaService ollamaDeepSeekService;

    public ChatController(OllamaService ollamaDeepSeekService) {
        this.ollamaDeepSeekService = ollamaDeepSeekService;
    }

    @GetMapping("")
    public String generateResponse() {
        String response = ollamaDeepSeekService.generateResponse("bir şaka söyle ");
        return response;
    }
    
}
