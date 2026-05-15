package com.resumeai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchResponse {
    private Long resumeId;
    private String resumeFileName;
    private List<JobMatch> matches;

    @Data
    @Builder
    public static class JobMatch {
        private Long jobId;
        private String jobTitle;
        private double skillScore;
        private double textSimilarityScore;
        private double finalScore;
        private String matchLabel; // e.g., "Excellent", "Good", "Fair", "Low"
        private LocalDateTime calculatedAt;
    }
}
