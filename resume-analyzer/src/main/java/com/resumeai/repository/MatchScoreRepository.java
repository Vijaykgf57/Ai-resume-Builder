package com.resumeai.repository;

import com.resumeai.entity.MatchScore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MatchScoreRepository extends MongoRepository<MatchScore, String> {
    List<MatchScore> findByResumeIdOrderByFinalScoreDesc(String resumeId);
    List<MatchScore> findByUserIdOrderByFinalScoreDesc(String userId);
    Optional<MatchScore> findByResumeIdAndJobId(String resumeId, String jobId);
}
