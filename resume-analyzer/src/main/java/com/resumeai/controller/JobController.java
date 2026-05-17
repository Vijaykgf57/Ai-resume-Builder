package com.resumeai.controller;

import com.resumeai.dto.JobRequest;
import com.resumeai.entity.Job;
import com.resumeai.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<Job> createJob(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(jobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }
}
