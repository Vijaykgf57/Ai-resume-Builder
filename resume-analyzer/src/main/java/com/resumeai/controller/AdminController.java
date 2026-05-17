package com.resumeai.controller;

import com.resumeai.dto.RegisterRequest;
import com.resumeai.entity.MatchScore;
import com.resumeai.entity.User;
import com.resumeai.repository.MatchScoreRepository;
import com.resumeai.repository.UserRepository;
import com.resumeai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final AuthService authService;

    /** GET /api/admin/users — list all registered users */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id",    u.getId());
                    m.put("name",  u.getName());
                    m.put("email", u.getEmail());
                    m.put("role",  u.getRole());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** GET /api/admin/matches — all match scores across all users */
    @GetMapping("/matches")
    public ResponseEntity<List<MatchScore>> getAllMatches() {
        List<MatchScore> all = matchScoreRepository.findAll()
                .stream()
                .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(all);
    }

    /** GET /api/admin/matches/{userId} — matches for a specific user */
    @GetMapping("/matches/{userId}")
    public ResponseEntity<List<MatchScore>> getMatchesForUser(@PathVariable String userId) {
        return ResponseEntity.ok(
                matchScoreRepository.findByUserIdOrderByFinalScoreDesc(userId)
        );
    }

    /** POST /api/admin/register — create another admin from within the app */
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }
}
