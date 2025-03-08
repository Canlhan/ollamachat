package com.ollamachat.ollamachat.Servies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(TextProcessingService.class);

    private final ResourceLoader resourceLoader;
    private final VectorService vectorService;

    @Autowired
    public TextProcessingService(ResourceLoader resourceLoader, VectorService vectorService) {
        this.resourceLoader = resourceLoader;
        this.vectorService = vectorService;
    }
    
    /**
     * Uygulama başladığında doc.txt dosyasını vektör deposuna ekler
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("Başlangıç dokümanları yükleniyor...");
            processAndStoreText("docs/doc.txt");
            logger.info("Başlangıç dokümanları başarıyla yüklendi");
        } catch (Exception e) {
            logger.error("Başlangıç dokümanları yüklenirken hata oluştu", e);
        }
    }

    /**
     * Belirtilen kaynak dosyasını okur, paragraflara böler ve vektör deposuna ekler
     */
    public void processAndStoreText(String resourcePath) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
            String text = readResourceContent(resource);
            List<String> paragraphs = splitIntoParagraphs(text);
            
            logger.info("Metin {} paragrafa bölündü", paragraphs.size());
            
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    logger.info("Paragraf vektör deposuna ekleniyor: {}", paragraph);
                    vectorService.addDocument(paragraph);
                }
            }
            
            logger.info("Tüm paragraflar vektör deposuna eklendi");
        } catch (IOException e) {
            logger.error("Metin işlenirken hata oluştu", e);
            throw new RuntimeException("Metin işlenirken hata oluştu", e);
        }
    }

    /**
     * Kaynak dosyasının içeriğini okur
     */
    private String readResourceContent(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Metni paragraflara böler
     */
    private List<String> splitIntoParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        String[] lines = text.split("\n");
        
        StringBuilder currentParagraph = new StringBuilder();
        
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (currentParagraph.length() > 0) {
                    paragraphs.add(currentParagraph.toString().trim());
                    currentParagraph = new StringBuilder();
                }
            } else {
                if (currentParagraph.length() > 0) {
                    currentParagraph.append(" ");
                }
                currentParagraph.append(line.trim());
            }
        }
        
        if (currentParagraph.length() > 0) {
            paragraphs.add(currentParagraph.toString().trim());
        }
        
        return paragraphs;
    }
} 