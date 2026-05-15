package com.resumeai.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * AI-based skill extractor using regex pattern matching + synonym resolution.
 * Extracts skills from raw resume text and assigns confidence scores.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillExtractor {

    private final SynonymDictionary synonymDictionary;

    // Known skill keywords to scan for in resume text
    private static final List<String> SKILL_KEYWORDS = List.of(
        // Languages
        "java", "python", "javascript", "typescript", "c\\+\\+", "c#", "kotlin", "scala", "go", "rust", "php", "ruby",
        // Frameworks
        "spring boot", "spring mvc", "spring", "hibernate", "django", "flask", "react", "angular", "vue",
        "node\\.js", "nodejs", "express", "fastapi",
        // Databases
        "mysql", "postgresql", "postgres", "mongodb", "redis", "oracle", "sql server", "cassandra", "elasticsearch",
        // Cloud & DevOps
        "aws", "azure", "gcp", "docker", "kubernetes", "k8s", "jenkins", "github actions", "gitlab ci",
        "terraform", "ansible",
        // Concepts
        "rest api", "restful", "graphql", "microservices", "soap", "web services",
        "jpa", "hibernate", "maven", "gradle",
        // Testing
        "junit", "mockito", "selenium", "testng",
        // Tools
        "git", "github", "gitlab", "bitbucket", "jira", "linux", "bash",
        // Methodologies
        "agile", "scrum", "kanban", "ci/cd", "devops"
    );

    /**
     * Extracts skills from resume text.
     * Returns a map of canonical skill name → confidence score (0.0 - 1.0).
     */
    public Map<String, Double> extractSkills(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return Collections.emptyMap();
        }

        String normalizedText = resumeText.toLowerCase();
        Map<String, Double> extractedSkills = new LinkedHashMap<>();

        for (String keyword : SKILL_KEYWORDS) {
            // Use word-boundary aware regex for accurate matching
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(normalizedText).find()) {
                String canonical = synonymDictionary.resolve(keyword.replace("\\.", ".").replace("\\+", "+"));
                double confidence = calculateConfidence(normalizedText, keyword);
                // Keep highest confidence if skill already found via synonym
                extractedSkills.merge(canonical, confidence, Math::max);
            }
        }

        log.debug("Extracted {} skills from resume text", extractedSkills.size());
        return extractedSkills;
    }

    /**
     * Calculates confidence score based on frequency and context.
     * Higher frequency = higher confidence (capped at 1.0).
     */
    private double calculateConfidence(String text, String keyword) {
        Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
        long count = pattern.matcher(text).results().count();

        // Base confidence: 0.7 for single mention, up to 1.0 for 3+ mentions
        if (count >= 3) return 1.0;
        if (count == 2) return 0.85;
        return 0.7;
    }
}
