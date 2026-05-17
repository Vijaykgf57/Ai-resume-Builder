package com.resumeai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchResponse {
    private String resumeId;
    private String resumeFileName;
    private List<JobMatch> matches;

    @Data
    @Builder
    public static class JobMatch {
        private String jobId;
        private String jobTitle;
        private double skillScore;
        private double textSimilarityScore;
        private double finalScore;
        private String matchLabel;
        private LocalDateTime calculatedAt;
    }
}
