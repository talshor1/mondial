package com.mondial.api.dto;

public record BetResponse(
        Long id,
        Long gameId,
        int homeGoals,
        int awayGoals,
        Integer points
) {}