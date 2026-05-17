package com.resumeai.ai;

import com.resumeai.entity.JobSkillEntry;
import com.resumeai.entity.SkillEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static final double MANDATORY_WEIGHT = 3.0;
    private static final double OPTIONAL_WEIGHT  = 1.0;
    private static final double SKILL_WEIGHT_FACTOR = 0.70;
    private static final double TEXT_WEIGHT_FACTOR  = 0.30;

    private final SynonymDictionary synonymDictionary;
    private final TfIdfCalculator tfIdfCalculator;

    public double computeSkillScore(List<SkillEntry> resumeSkills, List<JobSkillEntry> jobSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) return 0.0;

        Set<String> resumeCanonical = resumeSkills.stream()
                .map(s -> synonymDictionary.resolve(s.getSkillName()))
                .collect(Collectors.toSet());

        double totalWeight = 0.0;
        double matchedWeight = 0.0;

        for (JobSkillEntry js : jobSkills) {
            double weight = "MANDATORY".equalsIgnoreCase(js.getWeight()) ? MANDATORY_WEIGHT : OPTIONAL_WEIGHT;
            totalWeight += weight;

            String jobCanonical = synonymDictionary.resolve(js.getSkillName());
            boolean matched = resumeCanonical.stream()
                    .anyMatch(r -> r.equalsIgnoreCase(jobCanonical));

            if (matched) {
                matchedWeight += weight;
                log.debug("Matched: {} (weight={})", jobCanonical, weight);
            }
        }

        if (totalWeight == 0) return 0.0;
        return Math.round((matchedWeight / totalWeight) * 100.0 * 100.0) / 100.0;
    }

    public double computeTextSimilarityScore(String resumeText, String jobDescription) {
        return tfIdfCalculator.computeSimilarity(resumeText, jobDescription);
    }

    public double computeFinalScore(double skillScore, double textScore) {
        return Math.round(((skillScore * SKILL_WEIGHT_FACTOR) + (textScore * TEXT_WEIGHT_FACTOR)) * 100.0) / 100.0;
    }
}
