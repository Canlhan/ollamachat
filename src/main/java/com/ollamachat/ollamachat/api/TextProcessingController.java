package com.ollamachat.ollamachat.api;

import com.ollamachat.ollamachat.Servies.TextProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/text")
public class TextProcessingController {

    private final TextProcessingService textProcessingService;

    @Autowired
    public TextProcessingController(TextProcessingService textProcessingService) {
        this.textProcessingService = textProcessingService;
    }

    /**
     * Belirtilen kaynak dosyasını işler ve vektör deposuna ekler
     */
    @PostMapping("/process")
    public ResponseEntity<String> processText(@RequestParam String resourcePath) {
        try {
            textProcessingService.processAndStoreText(resourcePath);
            return ResponseEntity.ok("Metin başarıyla işlendi ve vektör deposuna kaydedildi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Metin işlenirken hata oluştu: " + e.getMessage());
        }
    }
    
    /**
     * doc.txt dosyasını işler ve vektör deposuna ekler
     */
    @GetMapping("/load-default")
    public ResponseEntity<String> loadDefaultText() {
        try {
            textProcessingService.processAndStoreText("docs/doc.txt");
            return ResponseEntity.ok("Varsayılan metin başarıyla işlendi ve vektör deposuna kaydedildi.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Varsayılan metin işlenirken hata oluştu: " + e.getMessage());
        }
    }
} 