package com.resumeai.entity;

import lombok.*;

/**
 * Embedded document — a skill with its confidence score.
 * Used inside Resume documents.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillEntry {
    private String skillName;
    private String category;
    private double confidenceScore;
}
