package com.resumeai.service;

import com.resumeai.ai.SkillExtractor;
import com.resumeai.ai.TextExtractor;
import com.resumeai.dto.ResumeResponse;
import com.resumeai.entity.*;
import com.resumeai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TextExtractor textExtractor;
    private final SkillExtractor skillExtractor;

    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Step 1: Extract text from file using Apache Tika
        String extractedText = textExtractor.extract(file);

        // Step 2: Build and save resume entity
        Resume resume = Resume.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .extractedText(extractedText)
                .build();
        resume = resumeRepository.save(resume);

        // Step 3: AI skill extraction
        Map<String, Double> extractedSkills = skillExtractor.extractSkills(extractedText);
        log.info("Extracted {} skills from resume: {}", extractedSkills.size(), file.getOriginalFilename());

        // Step 4: Persist skills and resume-skill links
        final Resume savedResume = resume;
        List<ResumeSkill> resumeSkills = extractedSkills.entrySet().stream()
                .map(entry -> {
                    Skill skill = findOrCreateSkill(entry.getKey());
                    return ResumeSkill.builder()
                            .resume(savedResume)
                            .skill(skill)
                            .confidenceScore(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());

        // Save via cascade by setting on resume
        savedResume.setResumeSkills(resumeSkills);
        resumeRepository.save(savedResume);

        return buildResumeResponse(savedResume, resumeSkills);
    }

    public List<ResumeResponse> getResumesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return resumeRepository.findByUserId(user.getId()).stream()
                .map(r -> buildResumeResponse(r, r.getResumeSkills()))
                .collect(Collectors.toList());
    }

    private Skill findOrCreateSkill(String skillName) {
        return skillRepository.findBySkillNameIgnoreCase(skillName)
                .orElseGet(() -> skillRepository.save(
                        Skill.builder()
                                .skillName(skillName)
                                .category("GENERAL")
                                .build()
                ));
    }

    private ResumeResponse buildResumeResponse(Resume resume, List<ResumeSkill> skills) {
        List<ResumeResponse.SkillInfo> skillInfos = skills == null ? List.of() :
                skills.stream()
                        .map(rs -> ResumeResponse.SkillInfo.builder()
                                .skillName(rs.getSkill().getSkillName())
                                .category(rs.getSkill().getCategory())
                                .confidenceScore(rs.getConfidenceScore())
                                .build())
                        .collect(Collectors.toList());

        return ResumeResponse.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .uploadedAt(resume.getUploadedAt())
                .extractedSkills(skillInfos)
                .build();
    }
}
