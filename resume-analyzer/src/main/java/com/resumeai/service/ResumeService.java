package com.resumeai.service;

import com.resumeai.ai.SkillExtractor;
import com.resumeai.ai.TextExtractor;
import com.resumeai.dto.ResumeResponse;
import com.resumeai.entity.Resume;
import com.resumeai.entity.SkillEntry;
import com.resumeai.entity.User;
import com.resumeai.repository.ResumeRepository;
import com.resumeai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final TextExtractor textExtractor;
    private final SkillExtractor skillExtractor;

    public ResumeResponse uploadResume(MultipartFile file, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Step 1: Extract text via Apache Tika
        String extractedText = textExtractor.extract(file);

        // Step 2: AI skill extraction
        Map<String, Double> skillMap = skillExtractor.extractSkills(extractedText);
        log.info("Extracted {} skills from: {}", skillMap.size(), file.getOriginalFilename());

        // Step 3: Build embedded skill list
        List<SkillEntry> skills = skillMap.entrySet().stream()
                .map(e -> SkillEntry.builder()
                        .skillName(e.getKey())
                        .category("GENERAL")
                        .confidenceScore(e.getValue())
                        .build())
                .collect(Collectors.toList());

        // Step 4: Save resume document
        Resume resume = Resume.builder()
                .userId(user.getId())
                .fileName(file.getOriginalFilename())
                .extractedText(extractedText)
                .extractedSkills(skills)
                .build();
        resume.prePersist();
        resume = resumeRepository.save(resume);

        return toResponse(resume);
    }

    public List<ResumeResponse> getResumesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return resumeRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ResumeResponse toResponse(Resume r) {
        List<ResumeResponse.SkillInfo> skillInfos = r.getExtractedSkills().stream()
                .map(s -> ResumeResponse.SkillInfo.builder()
                        .skillName(s.getSkillName())
                        .category(s.getCategory())
                        .confidenceScore(s.getConfidenceScore())
                        .build())
                .collect(Collectors.toList());

        return ResumeResponse.builder()
                .id(r.getId())
                .fileName(r.getFileName())
                .uploadedAt(r.getUploadedAt())
                .extractedSkills(skillInfos)
                .build();
    }
}
