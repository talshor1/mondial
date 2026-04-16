package com.mondial.api.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String role
) {
}

