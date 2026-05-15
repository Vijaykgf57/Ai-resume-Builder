package com.resumeai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_name", unique = true, nullable = false)
    private String skillName;

    private String category;

    @JsonIgnore
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private List<ResumeSkill> resumeSkills;

    @JsonIgnore
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL)
    private List<JobSkill> jobSkills;
}
