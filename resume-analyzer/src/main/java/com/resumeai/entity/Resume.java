package com.resumeai.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "resumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resume {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String fileName;

    private String extractedText;

    private LocalDateTime uploadedAt;

    @Builder.Default
    private List<SkillEntry> extractedSkills = new ArrayList<>();

    public void prePersist() {
        if (this.uploadedAt == null) this.uploadedAt = LocalDateTime.now();
    }
}
