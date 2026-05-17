package com.resumeai.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "match_scores")
@CompoundIndex(name = "resume_job_idx", def = "{'resumeId': 1, 'jobId': 1}", unique = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchScore {

    @Id
    private String id;

    private String resumeId;
    private String jobId;
    private String jobTitle;
    private String userId;
    private String userEmail;   // stored at match time for admin display
    private String resumeFileName;

    private double skillScore;
    private double textSimilarityScore;
    private double finalScore;

    private LocalDateTime calculatedAt;
}
