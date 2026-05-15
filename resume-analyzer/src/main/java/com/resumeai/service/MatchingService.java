package com.resumeai.service;

import com.resumeai.ai.MatchingEngine;
import com.resumeai.dto.MatchResponse;
import com.resumeai.entity.Job;
import com.resumeai.entity.JobSkill;
import com.resumeai.entity.MatchScore;
import com.resumeai.entity.Resume;
import com.resumeai.entity.ResumeSkill;
import com.resumeai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final MatchingEngine matchingEngine;

    /**
     * Computes match scores for a given resume against all jobs.
     * Saves/updates results in the database and returns ranked matches.
     */
    @Transactional
    public MatchResponse computeMatches(Long resumeId, String userEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found: " + resumeId));

        // Security: ensure resume belongs to requesting user
        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Access denied to resume: " + resumeId);
        }

        List<Job> allJobs = jobRepository.findAll();
        List<ResumeSkill> resumeSkills = resume.getResumeSkills();

        List<MatchResponse.JobMatch> jobMatches = allJobs.stream()
                .map(job -> computeAndSaveMatch(resume, job, resumeSkills))
                .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
                .collect(Collectors.toList());

        log.info("Computed {} matches for resume {}", jobMatches.size(), resumeId);

        return MatchResponse.builder()
                .resumeId(resumeId)
                .resumeFileName(resume.getFileName())
                .matches(jobMatches)
                .build();
    }

    /**
     * Returns previously computed matches for a user's resumes.
     */
    public List<MatchResponse.JobMatch> getMatchesForUser(String userEmail) {
        Long userId = resumeRepository.findAll().stream()
                .filter(r -> r.getUser().getEmail().equals(userEmail))
                .map(r -> r.getUser().getId())
                .findFirst()
                .orElse(-1L);

        return matchScoreRepository.findByUserIdOrderByScoreDesc(userId)
                .stream().map(this::toJobMatch).collect(Collectors.toList());
    }

    private MatchResponse.JobMatch computeAndSaveMatch(Resume resume, Job job, List<ResumeSkill> resumeSkills) {
        List<JobSkill> jobSkills = job.getJobSkills();

        double skillScore = matchingEngine.computeSkillScore(resumeSkills, jobSkills);
        double textScore  = matchingEngine.computeTextSimilarityScore(
                resume.getExtractedText(), job.getDescription()
        );
        double finalScore = matchingEngine.computeFinalScore(skillScore, textScore);

        // Upsert match score
        MatchScore matchScore = matchScoreRepository
                .findByResumeIdAndJobId(resume.getId(), job.getId())
                .orElse(MatchScore.builder().resume(resume).job(job).build());

        matchScore.setSkillScore(skillScore);
        matchScore.setTextSimilarityScore(textScore);
        matchScore.setFinalScore(finalScore);
        matchScoreRepository.save(matchScore);

        log.debug("Match: {} → {} | skill={} text={} final={}",
                resume.getFileName(), job.getTitle(), skillScore, textScore, finalScore);

        return MatchResponse.JobMatch.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .skillScore(skillScore)
                .textSimilarityScore(textScore)
                .finalScore(finalScore)
                .matchLabel(getMatchLabel(finalScore))
                .calculatedAt(matchScore.getCalculatedAt())
                .build();
    }

    private MatchResponse.JobMatch toJobMatch(MatchScore ms) {
        return MatchResponse.JobMatch.builder()
                .jobId(ms.getJob().getId())
                .jobTitle(ms.getJob().getTitle())
                .skillScore(ms.getSkillScore())
                .textSimilarityScore(ms.getTextSimilarityScore())
                .finalScore(ms.getFinalScore())
                .matchLabel(getMatchLabel(ms.getFinalScore()))
                .calculatedAt(ms.getCalculatedAt())
                .build();
    }

    /**
     * Converts a numeric score to a human-readable label.
     */
    private String getMatchLabel(double score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Good";
        if (score >= 40) return "Fair";
        return "Low";
    }
}
