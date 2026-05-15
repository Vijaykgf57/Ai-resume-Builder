package com.resumeai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    /**
     * Confidence score (0.0 - 1.0) indicating how confidently
     * this skill was extracted from the resume text.
     */
    @Column(name = "confidence_score")
    private Double confidenceScore;
}
