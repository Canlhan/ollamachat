package com.ollamachat.ollamachat.Servies;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class OllamaService {
    
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    
    private final ChatClient chatClient;

    public OllamaService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }
    
    public String generateResponse(String userInput) {
        logger.debug("generateResponse metodu çağrıldı. Kullanıcı girişi: {}", userInput);
        try {
            var systemMessage = new SystemMessage("You are a helpful assistant that can answer questions and help with tasks.");
            Prompt prompt = new Prompt(systemMessage, new UserMessage(userInput));
            logger.debug("Prompt oluşturuldu: {}", prompt);
            
            String response = chatClient.prompt().user(userInput).call().content();
            logger.debug("Alınan yanıt: {}", response);
            
            return response;
        } catch (Exception e) {
            logger.error("Hata oluştu: ", e);
            return "Bir hata oluştu: " + e.getMessage();
        }
    }
    
    public String getModelInfo() {
        return "DeepSeek-R1 - Ollama üzerinden çalışan güçlü bir dil modeli";
    }

    public Flux<String> streamResponse(String userInput) {
        return 
        chatClient.prompt()
        .user(userInput)
        .stream()
        .content();
                
    }
}
