package com.resumeai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class JobRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private List<JobSkillRequest> skills;

    @Data
    public static class JobSkillRequest {
        @NotBlank
        private String skillName;
        private String category;
        private String weight; // "MANDATORY" or "OPTIONAL"
    }
}
