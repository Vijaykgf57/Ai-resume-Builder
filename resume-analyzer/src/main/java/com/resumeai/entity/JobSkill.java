package com.resumeai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnoreProperties({"jobSkills", "matchScores"})
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    @JsonIgnoreProperties({"resumeSkills", "jobSkills"})
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillWeight weight;

    public enum SkillWeight {
        MANDATORY,
        OPTIONAL
    }
}
