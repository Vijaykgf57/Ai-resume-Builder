package com.resumeai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private String fileName;
    private LocalDateTime uploadedAt;
    private List<SkillInfo> extractedSkills;

    @Data
    @Builder
    public static class SkillInfo {
        private String skillName;
        private String category;
        private double confidenceScore;
    }
}
