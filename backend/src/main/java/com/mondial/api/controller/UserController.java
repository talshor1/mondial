package com.mondial.api.controller;

import com.mondial.api.dto.UserMeResponse;
import com.mondial.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getTeamName()));
    }

    @PutMapping("/me/team")
    public ResponseEntity<UserMeResponse> updateTeamName(Authentication authentication,
                                                          @RequestBody Map<String, String> body) {
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        String teamName = body.get("teamName");
        if (teamName == null || teamName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        user.setTeamName(teamName.trim());
        userRepository.save(user);

        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getTeamName()));
    }
}