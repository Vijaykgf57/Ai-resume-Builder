package com.resumeai.service;

import com.resumeai.ai.MatchingEngine;
import com.resumeai.dto.MatchResponse;
import com.resumeai.entity.*;
import com.resumeai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final UserRepository userRepository;
    private final MatchingEngine matchingEngine;

    public MatchResponse computeMatches(String resumeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

        if (!resume.getUserId().equals(user.getId())) {
            throw new SecurityException("Access denied to resume: " + resumeId);
        }

        List<Job> allJobs = jobRepository.findAll();

        List<MatchResponse.JobMatch> jobMatches = allJobs.stream()
                .map(job -> computeAndSave(resume, job, user.getId()))
                .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
                .collect(Collectors.toList());

        log.info("Computed {} matches for resume {}", jobMatches.size(), resumeId);

        return MatchResponse.builder()
                .resumeId(resumeId)
                .resumeFileName(resume.getFileName())
                .matches(jobMatches)
                .build();
    }

    public List<MatchResponse.JobMatch> getMatchesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return matchScoreRepository.findByUserIdOrderByFinalScoreDesc(user.getId())
                .stream().map(this::toJobMatch).collect(Collectors.toList());
    }

    private MatchResponse.JobMatch computeAndSave(Resume resume, Job job, String userId) {
        String userEmail = userRepository.findById(userId).map(u -> u.getEmail()).orElse("unknown");
        double skillScore = matchingEngine.computeSkillScore(resume.getExtractedSkills(), job.getJobSkills());
        double textScore  = matchingEngine.computeTextSimilarityScore(resume.getExtractedText(), job.getDescription());
        double finalScore = matchingEngine.computeFinalScore(skillScore, textScore);

        // Upsert
        MatchScore ms = matchScoreRepository
                .findByResumeIdAndJobId(resume.getId(), job.getId())
                .orElse(MatchScore.builder()
                        .resumeId(resume.getId())
                        .jobId(job.getId())
                        .userId(userId)
                        .userEmail(userEmail)
                        .resumeFileName(resume.getFileName())
                        .build());

        ms.setJobTitle(job.getTitle());
        ms.setSkillScore(skillScore);
        ms.setTextSimilarityScore(textScore);
        ms.setFinalScore(finalScore);
        ms.setCalculatedAt(LocalDateTime.now());
        matchScoreRepository.save(ms);

        return toJobMatch(ms);
    }

    private MatchResponse.JobMatch toJobMatch(MatchScore ms) {
        return MatchResponse.JobMatch.builder()
                .jobId(ms.getJobId())
                .jobTitle(ms.getJobTitle())
                .skillScore(ms.getSkillScore())
                .textSimilarityScore(ms.getTextSimilarityScore())
                .finalScore(ms.getFinalScore())
                .matchLabel(getLabel(ms.getFinalScore()))
                .calculatedAt(ms.getCalculatedAt())
                .build();
    }

    private String getLabel(double score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        return "Low";
    }
}
