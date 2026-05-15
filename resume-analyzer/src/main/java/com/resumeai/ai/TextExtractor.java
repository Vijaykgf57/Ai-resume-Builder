package com.resumeai.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts plain text from uploaded resume files (PDF, DOCX, TXT, etc.)
 * using Apache Tika.
 */
@Slf4j
@Component
public class TextExtractor {

    private final Tika tika = new Tika();

    /**
     * Extracts text content from a multipart file.
     */
    public String extract(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String text = tika.parseToString(inputStream);
            log.debug("Extracted {} characters from file: {}", text.length(), file.getOriginalFilename());
            return text;
        } catch (IOException | TikaException e) {
            log.error("Failed to extract text from file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Could not extract text from resume file: " + e.getMessage());
        }
    }
}
