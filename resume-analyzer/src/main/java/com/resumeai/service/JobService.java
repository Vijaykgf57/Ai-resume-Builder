package com.resumeai.service;

import com.resumeai.dto.JobRequest;
import com.resumeai.entity.Job;
import com.resumeai.entity.JobSkillEntry;
import com.resumeai.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public Job createJob(JobRequest request) {
        List<JobSkillEntry> skills = (request.getSkills() == null) ? List.of() :
                request.getSkills().stream()
                        .map(s -> JobSkillEntry.builder()
                                .skillName(s.getSkillName())
                                .category(s.getCategory())
                                .weight(s.getWeight() != null ? s.getWeight().toUpperCase() : "OPTIONAL")
                                .build())
                        .collect(Collectors.toList());

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .jobSkills(skills)
                .build();

        job = jobRepository.save(job);
        log.info("Created job '{}' with {} skills", job.getTitle(), skills.size());
        return job;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }
}
