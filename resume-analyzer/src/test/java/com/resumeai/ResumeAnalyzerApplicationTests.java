package com.resumeai;

import com.resumeai.ai.MatchingEngine;
import com.resumeai.ai.SynonymDictionary;
import com.resumeai.ai.TfIdfCalculator;
import com.resumeai.entity.JobSkillEntry;
import com.resumeai.entity.SkillEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumeAnalyzerApplicationTests {

    private final SynonymDictionary synonymDictionary = new SynonymDictionary();
    private final TfIdfCalculator tfIdfCalculator = new TfIdfCalculator();
    private final MatchingEngine matchingEngine = new MatchingEngine(synonymDictionary, tfIdfCalculator);

    @Test
    void synonymResolution_springBootMapsToSpring() {
        assertEquals("Spring", synonymDictionary.resolve("Spring Boot"));
        assertEquals("Spring", synonymDictionary.resolve("spring"));
        assertEquals("Spring", synonymDictionary.resolve("Spring MVC"));
    }

    @Test
    void synonymResolution_restApiVariants() {
        assertEquals("REST API", synonymDictionary.resolve("REST"));
        assertEquals("REST API", synonymDictionary.resolve("RESTful"));
        assertEquals("REST API", synonymDictionary.resolve("Web Services"));
    }

    @Test
    void skillScore_perfectMatch() {
        List<SkillEntry> resumeSkills = List.of(
                skill("Java"), skill("Spring Boot")
        );
        List<JobSkillEntry> jobSkills = List.of(
                jobSkill("Java", "MANDATORY"),
                jobSkill("Spring", "MANDATORY")
        );
        double score = matchingEngine.computeSkillScore(resumeSkills, jobSkills);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void skillScore_partialMatch() {
        List<SkillEntry> resumeSkills = List.of(skill("Java"));
        List<JobSkillEntry> jobSkills = List.of(
                jobSkill("Java", "MANDATORY"),
                jobSkill("Spring", "MANDATORY"),
                jobSkill("REST API", "OPTIONAL")
        );
        double score = matchingEngine.computeSkillScore(resumeSkills, jobSkills);
        assertTrue(score > 40 && score < 45, "Expected ~42.86, got: " + score);
    }

    @Test
    void textSimilarity_similarTexts() {
        String resume = "Experienced Java developer with Spring Boot and REST API development";
        String job    = "Looking for Java developer with Spring framework and REST API skills";
        double score = tfIdfCalculator.computeSimilarity(resume, job);
        assertTrue(score > 30, "Expected > 30%, got: " + score);
    }

    @Test
    void textSimilarity_differentTexts() {
        String resume = "Java Spring Boot developer";
        String job    = "Graphic designer with Photoshop and Illustrator skills";
        double score = tfIdfCalculator.computeSimilarity(resume, job);
        assertTrue(score < 20, "Expected < 20%, got: " + score);
    }

    @Test
    void finalScore_combinedCorrectly() {
        double finalScore = matchingEngine.computeFinalScore(80.0, 60.0);
        assertEquals(74.0, finalScore, 0.01);
    }

    private SkillEntry skill(String name) {
        return SkillEntry.builder().skillName(name).confidenceScore(1.0).build();
    }

    private JobSkillEntry jobSkill(String name, String weight) {
        return JobSkillEntry.builder().skillName(name).weight(weight).build();
    }
}
