package com.resumeai.entity;

import lombok.*;

/**
 * Embedded document — a skill requirement inside a Job document.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobSkillEntry {
    private String skillName;
    private String category;
    private String weight; // "MANDATORY" or "OPTIONAL"
}
