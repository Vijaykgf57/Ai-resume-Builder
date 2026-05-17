package com.resumeai.repository;

import com.resumeai.entity.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByTitleContainingIgnoreCase(String title);
}
