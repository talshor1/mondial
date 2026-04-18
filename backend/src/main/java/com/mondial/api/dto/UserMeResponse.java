package com.mondial.api.dto;

public record UserMeResponse(
        Long id,
        String email,
        String role,
        String teamName
) {
}

