package com.resumeai.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF based cosine similarity calculator for text comparison.
 * Used to compute similarity between resume text and job description.
 */
@Component
public class TfIdfCalculator {

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "shall", "can", "need", "dare", "ought",
        "used", "i", "you", "he", "she", "it", "we", "they", "this", "that",
        "these", "those", "my", "your", "his", "her", "its", "our", "their"
    );

    /**
     * Computes cosine similarity between two texts using TF-IDF vectors.
     * Returns a score between 0.0 and 100.0.
     */
    public double computeSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isBlank() || text2.isBlank()) {
            return 0.0;
        }

        List<String> tokens1 = tokenize(text1);
        List<String> tokens2 = tokenize(text2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

        // Build vocabulary from both documents
        Set<String> vocabulary = new HashSet<>(tokens1);
        vocabulary.addAll(tokens2);

        // Compute TF for each document
        Map<String, Double> tf1 = computeTF(tokens1);
        Map<String, Double> tf2 = computeTF(tokens2);

        // Compute IDF using both documents as the corpus
        Map<String, Double> idf = computeIDF(vocabulary, List.of(tokens1, tokens2));

        // Build TF-IDF vectors
        Map<String, Double> tfidf1 = computeTFIDF(tf1, idf, vocabulary);
        Map<String, Double> tfidf2 = computeTFIDF(tf2, idf, vocabulary);

        double cosine = cosineSimilarity(tfidf1, tfidf2, vocabulary);
        return Math.round(cosine * 100.0 * 100.0) / 100.0; // return as percentage, 2 decimal places
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-zA-Z0-9+#.]+"))
                .filter(t -> t.length() > 1)
                .filter(t -> !STOP_WORDS.contains(t))
                .collect(Collectors.toList());
    }

    private Map<String, Double> computeTF(List<String> tokens) {
        Map<String, Long> freq = new HashMap<>();
        for (String token : tokens) {
            freq.merge(token, 1L, Long::sum);
        }
        Map<String, Double> tf = new HashMap<>();
        int total = tokens.size();
        freq.forEach((term, count) -> tf.put(term, (double) count / total));
        return tf;
    }

    private Map<String, Double> computeIDF(Set<String> vocabulary, List<List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        int numDocs = documents.size();
        for (String term : vocabulary) {
            long docsContaining = documents.stream()
                    .filter(doc -> doc.contains(term))
                    .count();
            // Smoothed IDF
            idf.put(term, Math.log((double)(numDocs + 1) / (docsContaining + 1)) + 1);
        }
        return idf;
    }

    private Map<String, Double> computeTFIDF(Map<String, Double> tf, Map<String, Double> idf, Set<String> vocabulary) {
        Map<String, Double> tfidf = new HashMap<>();
        for (String term : vocabulary) {
            double tfVal = tf.getOrDefault(term, 0.0);
            double idfVal = idf.getOrDefault(term, 0.0);
            tfidf.put(term, tfVal * idfVal);
        }
        return tfidf;
    }

    private double cosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2, Set<String> vocabulary) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : vocabulary) {
            double v1 = vec1.getOrDefault(term, 0.0);
            double v2 = vec2.getOrDefault(term, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
