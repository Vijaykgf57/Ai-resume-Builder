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

    @PostMapping("/{resumeId}")
    public ResponseEntity<MatchResponse> computeMatches(
            @PathVariable String resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(matchingService.computeMatches(resumeId, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse.JobMatch>> getMyMatches(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(matchingService.getMatchesForUser(userDetails.getUsername()));
    }
}
