package com.resumeai.ai;

import com.resumeai.entity.JobSkill;
import com.resumeai.entity.ResumeSkill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core matching engine that computes weighted skill scores
 * and combines them with text similarity for a final match score.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static final double MANDATORY_WEIGHT = 3.0;
    private static final double OPTIONAL_WEIGHT  = 1.0;

    // Weight split: 70% skill match, 30% text similarity
    private static final double SKILL_WEIGHT_FACTOR = 0.70;
    private static final double TEXT_WEIGHT_FACTOR  = 0.30;

    private final SynonymDictionary synonymDictionary;
    private final TfIdfCalculator tfIdfCalculator;

    /**
     * Computes the weighted skill match score (0-100).
     *
     * Formula: (sum of weights of matched skills / total possible weight) * 100
     */
    public double computeSkillScore(List<ResumeSkill> resumeSkills, List<JobSkill> jobSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) return 0.0;

        // Canonical skill names from resume
        Set<String> resumeCanonicalSkills = resumeSkills.stream()
                .map(rs -> synonymDictionary.resolve(rs.getSkill().getSkillName()))
                .collect(Collectors.toSet());

        double totalWeight = 0.0;
        double matchedWeight = 0.0;

        for (JobSkill jobSkill : jobSkills) {
            double weight = jobSkill.getWeight() == JobSkill.SkillWeight.MANDATORY
                    ? MANDATORY_WEIGHT : OPTIONAL_WEIGHT;
            totalWeight += weight;

            String jobCanonical = synonymDictionary.resolve(jobSkill.getSkill().getSkillName());

            // Check if resume contains this skill (synonym-aware)
            boolean matched = resumeCanonicalSkills.stream()
                    .anyMatch(rs -> rs.equalsIgnoreCase(jobCanonical));

            if (matched) {
                matchedWeight += weight;
                log.debug("Skill matched: {} (weight={})", jobCanonical, weight);
            } else {
                log.debug("Skill NOT matched: {}", jobCanonical);
            }
        }

        if (totalWeight == 0) return 0.0;
        double score = (matchedWeight / totalWeight) * 100.0;
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Computes text similarity score between resume and job description (0-100).
     */
    public double computeTextSimilarityScore(String resumeText, String jobDescription) {
        return tfIdfCalculator.computeSimilarity(resumeText, jobDescription);
    }

    /**
     * Combines skill score and text similarity into a final score.
     * 70% skill match + 30% text similarity.
     */
    public double computeFinalScore(double skillScore, double textSimilarityScore) {
        double final_ = (skillScore * SKILL_WEIGHT_FACTOR) + (textSimilarityScore * TEXT_WEIGHT_FACTOR);
        return Math.round(final_ * 100.0) / 100.0;
    }
}
