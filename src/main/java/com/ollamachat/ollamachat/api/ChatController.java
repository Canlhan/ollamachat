package com.ollamachat.ollamachat.api;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ollamachat.ollamachat.Servies.OllamaService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final OllamaService ollamaDeepSeekService;

    public ChatController(OllamaService ollamaDeepSeekService) {
        this.ollamaDeepSeekService = ollamaDeepSeekService;
    }

    @PostMapping("/generate")
    public String generateResponse(@RequestBody String userInput) {
        String response = ollamaDeepSeekService.generateResponse(userInput);
        return response;
    }

    @PostMapping("/stream")
    public Flux<String> streamResponse(@RequestBody String userInput) {
        return ollamaDeepSeekService.streamResponse(userInput);
    }
    
}
