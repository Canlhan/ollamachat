package com.ollamachat.ollamachat.Servies;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class OllamaService {
    
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    
    private final ChatClient chatClient;
    private final VectorService vectorService;
    private final VectorStore vectorStore;
    
    public OllamaService(ChatClient.Builder chatClient, VectorService vectorService, VectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.vectorService = vectorService;
        this.vectorStore = vectorStore;
    }
    
    public String generateResponse(String userInput) {
        logger.debug("generateResponse metodu çağrıldı. Kullanıcı girişi: {}", userInput);
        try {
            // Özel karakterleri temizle
            String sanitizedInput = sanitizeInput(userInput);
            
            // Kullanıcı sorgularını vektör deposuna eklemeyi devre dışı bırak
            // vectorService.addDocument(sanitizedInput);
            
            // Benzer belgeleri getir
            List<Document> relevantDocs = retrieveRelevantDocuments(sanitizedInput);
            
            // Belgeleri birleştir
            String context = formatDocumentsAsContext(relevantDocs);
            logger.debug("Oluşturulan bağlam: {}", context);
            
            // Sistem mesajı oluştur
            String systemPrompt = "Aşağıdaki bilgileri kullanarak kullanıcının sorusuna cevap ver. Eğer cevap bilgilerde yoksa, bilmediğini söyle.\n\n" + context;
            
            // Yanıt oluştur
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(sanitizedInput)
                    .call()
                    .content();
            
            logger.debug("Alınan yanıt: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Hata oluştu: ", e);
            return "Bir hata oluştu: " + e.getMessage();
        }
    }
    
    public Flux<String> streamResponse(String userInput) {
        try {
            logger.debug("streamResponse metodu çağrıldı. Kullanıcı girişi: {}", userInput);
            
            // Özel karakterleri temizle
            String sanitizedInput = sanitizeInput(userInput);
            
            // Kullanıcı sorgularını vektör deposuna eklemeyi devre dışı bırak
            // vectorService.addDocument(sanitizedInput);
            
            // Benzer belgeleri getir
            List<Document> relevantDocs = retrieveRelevantDocuments(sanitizedInput);
            
            // Belgeleri birleştir
            String context = formatDocumentsAsContext(relevantDocs);
            logger.debug("Oluşturulan bağlam: {}", context);
            
            // Sistem mesajı oluştur
            String systemPrompt = "Aşağıdaki bilgileri kullanarak kullanıcının sorusuna cevap ver. Eğer cevap bilgilerde yoksa, bilmediğini söyle.\n\n" + context;
            
            // Stream yanıt oluştur
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(sanitizedInput)
                    .stream()
                    .content();
        } catch (Exception e) {
            logger.error("Stream hatası: ", e);
            return Flux.error(e);
        }
    }
    
    private List<Document> retrieveRelevantDocuments(String query) {
        try {
            // Vektör deposundan benzer belgeleri getir
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(5)  // En benzer 5 belgeyi getir
                    .similarityThreshold(0.5f)  // Benzerlik eşiğini düşür
                    .build();
            
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            logger.debug("Bulunan belge sayısı: {}", documents.size());
            return documents;
        } catch (Exception e) {
            logger.error("Belge arama hatası: ", e);
            return new ArrayList<>();
        }
    }
    
    private String formatDocumentsAsContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "Hiçbir ilgili bilgi bulunamadı.";
        }
        
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("İlgili bilgiler:\n\n");
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String content = doc.getText();
            
            // Kullanıcı sorgularını filtrele
            if (content.contains("userinput")) {
                continue;
            }
            
            contextBuilder.append(i + 1).append(". ").append(content).append("\n\n");
        }
        
        return contextBuilder.toString();
    }
    
    // Özel karakterleri ve şablon işleme sorunlarına neden olabilecek karakterleri temizle
    private String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        // JSON formatını temizle
        if (input.contains("{") && input.contains("}") && input.contains("userinput")) {
            try {
                // JSON formatını daha güvenilir bir şekilde işle
                int startIndex = input.indexOf("\"userinput\":\"") + "\"userinput\":\"".length();
                int endIndex = input.lastIndexOf("\"");
                if (startIndex > 0 && endIndex > startIndex) {
                    input = input.substring(startIndex, endIndex);
                    logger.debug("JSON formatı temizlendi. Yeni girdi: {}", input);
                } else {
                    // Regex ile dene
                    input = input.replaceAll("\\{\"userinput\":\"", "")
                                .replaceAll("\"\\}", "");
                    logger.debug("JSON formatı regex ile temizlendi. Yeni girdi: {}", input);
                }
            } catch (Exception e) {
                logger.error("JSON formatı temizlenirken hata oluştu: ", e);
                // Regex ile dene
                input = input.replaceAll("\\{\"userinput\":\"", "")
                            .replaceAll("\"\\}", "");
            }
        }
        
        // Şablon işleme sorunlarına neden olabilecek karakterleri temizle
        return input.replaceAll("\\$", " ")
                   .replaceAll("\\{", " ")
                   .replaceAll("\\}", " ")
                   .replaceAll("\\\\", " ");
    }
}
