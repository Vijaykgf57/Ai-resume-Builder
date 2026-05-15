package com.resumeai.controller;

import com.resumeai.dto.ResumeResponse;
import com.resumeai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * POST /api/resume/upload
     * Upload a resume file (PDF, DOCX, TXT). System extracts text and skills automatically.
     */
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ResumeResponse response = resumeService.uploadResume(file, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/resume
     * Get all resumes for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(resumeService.getResumesForUser(userDetails.getUsername()));
    }
}
