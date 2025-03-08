package com.ollamachat.ollamachat.Servies;

import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorService.class);
   
    private final VectorStore vectorStore;

    public VectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
   
    /**
     * Belgeyi vektör deposuna ekler
     */
    public void addDocument(String content) {
        try {
            logger.debug("addDocument metodu çağrıldı. İçerik: {}", content);
            List<Document> documents = new ArrayList<>();
            documents.add(new Document(content));
            vectorStore.add(documents);
            logger.debug("Belge vektör deposuna eklendi");
        } catch (Exception e) {
            logger.error("Belge eklenirken hata oluştu: ", e);
            throw e;
        }
    }
    
    /**
     * Sorguya benzer belgeleri getirir
     */
    public List<Document> getDocuments(String query) {
        try {
            logger.debug("getDocuments metodu çağrıldı. Sorgu: {}", query);
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(5)
                    .build();
            
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            logger.debug("Bulunan belge sayısı: {}", documents.size());
            
            return documents;
        } catch (Exception e) {
            logger.error("Belge aranırken hata oluştu: ", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Belgeleri metin olarak birleştirir
     */
    public String getDocumentsAsText(String query) {
        List<Document> documents = getDocuments(query);
        if (documents.isEmpty()) {
            return "Hiçbir ilgili bilgi bulunamadı.";
        }
        
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }
}
