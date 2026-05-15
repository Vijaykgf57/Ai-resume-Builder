package com.resumeai;

import com.resumeai.ai.MatchingEngine;
import com.resumeai.ai.SynonymDictionary;
import com.resumeai.ai.TfIdfCalculator;
import com.resumeai.entity.JobSkill;
import com.resumeai.entity.ResumeSkill;
import com.resumeai.entity.Skill;
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
        // Job requires Java (MANDATORY) and Spring (MANDATORY)
        // Resume has Java and Spring Boot (synonym for Spring)
        List<ResumeSkill> resumeSkills = List.of(
                makeResumeSkill("Java"),
                makeResumeSkill("Spring Boot")
        );
        List<JobSkill> jobSkills = List.of(
                makeJobSkill("Java", JobSkill.SkillWeight.MANDATORY),
                makeJobSkill("Spring", JobSkill.SkillWeight.MANDATORY)
        );

        double score = matchingEngine.computeSkillScore(resumeSkills, jobSkills);
        assertEquals(100.0, score, 0.01, "Spring Boot should match Spring via synonym");
    }

    @Test
    void skillScore_partialMatch() {
        // Job: Java (MANDATORY, w=3), Spring (MANDATORY, w=3), REST API (OPTIONAL, w=1) → total=7
        // Resume: Java only → matched=3 → score = 3/7 * 100 ≈ 42.86
        List<ResumeSkill> resumeSkills = List.of(makeResumeSkill("Java"));
        List<JobSkill> jobSkills = List.of(
                makeJobSkill("Java", JobSkill.SkillWeight.MANDATORY),
                makeJobSkill("Spring", JobSkill.SkillWeight.MANDATORY),
                makeJobSkill("REST API", JobSkill.SkillWeight.OPTIONAL)
        );

        double score = matchingEngine.computeSkillScore(resumeSkills, jobSkills);
        assertTrue(score > 40 && score < 45, "Partial match score should be ~42.86, got: " + score);
    }

    @Test
    void textSimilarity_similarTexts() {
        String resume = "Experienced Java developer with Spring Boot and REST API development";
        String job    = "Looking for Java developer with Spring framework and REST API skills";
        double score = tfIdfCalculator.computeSimilarity(resume, job);
        assertTrue(score > 30, "Similar texts should have similarity > 30%, got: " + score);
    }

    @Test
    void textSimilarity_differentTexts() {
        String resume = "Java Spring Boot developer";
        String job    = "Graphic designer with Photoshop and Illustrator skills";
        double score = tfIdfCalculator.computeSimilarity(resume, job);
        assertTrue(score < 20, "Unrelated texts should have low similarity, got: " + score);
    }

    @Test
    void finalScore_combinedCorrectly() {
        double finalScore = matchingEngine.computeFinalScore(80.0, 60.0);
        // 80 * 0.70 + 60 * 0.30 = 56 + 18 = 74
        assertEquals(74.0, finalScore, 0.01);
    }

    // --- helpers ---

    private ResumeSkill makeResumeSkill(String name) {
        Skill skill = new Skill();
        skill.setSkillName(name);
        ResumeSkill rs = new ResumeSkill();
        rs.setSkill(skill);
        rs.setConfidenceScore(1.0);
        return rs;
    }

    private JobSkill makeJobSkill(String name, JobSkill.SkillWeight weight) {
        Skill skill = new Skill();
        skill.setSkillName(name);
        JobSkill js = new JobSkill();
        js.setSkill(skill);
        js.setWeight(weight);
        return js;
    }
}
