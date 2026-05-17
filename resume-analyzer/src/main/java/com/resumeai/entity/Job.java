package com.resumeai.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Job {

    @Id
    private String id;

    private String title;

    private String description;

    @Builder.Default
    private List<JobSkillEntry> jobSkills = new ArrayList<>();
}
