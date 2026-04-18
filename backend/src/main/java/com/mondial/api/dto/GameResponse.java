package com.mondial.api.dto;

import java.time.OffsetDateTime;

public record GameResponse(
        Long id,
        String homeTeam,
        String awayTeam,
        OffsetDateTime startsAt,
        String status,
        Integer homeScore,
        Integer awayScore
) {}