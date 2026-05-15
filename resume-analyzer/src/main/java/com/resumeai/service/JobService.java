package com.resumeai.service;

import com.resumeai.dto.JobRequest;
import com.resumeai.entity.Job;
import com.resumeai.entity.JobSkill;
import com.resumeai.entity.Skill;
import com.resumeai.repository.JobRepository;
import com.resumeai.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public Job createJob(JobRequest request) {
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        job = jobRepository.save(job);

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            final Job savedJob = job;
            List<JobSkill> jobSkills = request.getSkills().stream()
                    .map(skillReq -> {
                        Skill skill = findOrCreateSkill(skillReq.getSkillName(), skillReq.getCategory());
                        JobSkill.SkillWeight weight = parseWeight(skillReq.getWeight());
                        return JobSkill.builder()
                                .job(savedJob)
                                .skill(skill)
                                .weight(weight)
                                .build();
                    })
                    .collect(Collectors.toList());

            savedJob.setJobSkills(jobSkills);
            job = jobRepository.save(savedJob);
        }

        log.info("Created job: {} with {} skills", job.getTitle(),
                job.getJobSkills() != null ? job.getJobSkills().size() : 0);
        return job;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    private Skill findOrCreateSkill(String skillName, String category) {
        return skillRepository.findBySkillNameIgnoreCase(skillName)
                .orElseGet(() -> skillRepository.save(
                        Skill.builder()
                                .skillName(skillName)
                                .category(category != null ? category : "GENERAL")
                                .build()
                ));
    }

    private JobSkill.SkillWeight parseWeight(String weight) {
        if (weight == null) return JobSkill.SkillWeight.OPTIONAL;
        try {
            return JobSkill.SkillWeight.valueOf(weight.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JobSkill.SkillWeight.OPTIONAL;
        }
    }
}
