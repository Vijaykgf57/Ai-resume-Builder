package com.resumeai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /** Weighted skill match score (0-100) */
    @Column(name = "skill_score")
    private Double skillScore;

    /** Text similarity score using TF-IDF cosine similarity (0-100) */
    @Column(name = "text_similarity_score")
    private Double textSimilarityScore;

    /** Final combined score (0-100) */
    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.calculatedAt = LocalDateTime.now();
    }
}
