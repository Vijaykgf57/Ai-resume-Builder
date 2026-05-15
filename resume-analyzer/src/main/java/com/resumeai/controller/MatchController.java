package com.resumeai.controller;

import com.resumeai.dto.MatchResponse;
import com.resumeai.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;

    /**
     * POST /api/matches/{resumeId}
     * Trigger AI matching for a specific resume against all jobs.
     * Returns ranked job matches with scores.
     */
    @PostMapping("/{resumeId}")
    public ResponseEntity<MatchResponse> computeMatches(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MatchResponse response = matchingService.computeMatches(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/matches
     * Get all previously computed matches for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<MatchResponse.JobMatch>> getMyMatches(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(matchingService.getMatchesForUser(userDetails.getUsername()));
    }
}
