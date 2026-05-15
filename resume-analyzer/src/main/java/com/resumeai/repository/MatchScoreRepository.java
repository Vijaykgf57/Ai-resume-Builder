package com.resumeai.repository;

import com.resumeai.entity.MatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchScoreRepository extends JpaRepository<MatchScore, Long> {

    List<MatchScore> findByResumeIdOrderByFinalScoreDesc(Long resumeId);

    Optional<MatchScore> findByResumeIdAndJobId(Long resumeId, Long jobId);

    @Query("SELECT m FROM MatchScore m WHERE m.resume.user.id = :userId ORDER BY m.finalScore DESC")
    List<MatchScore> findByUserIdOrderByScoreDesc(Long userId);
}
