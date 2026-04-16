package com.mondial.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BetResponse(
        Long id,
        Long gameId,
        String gameName,
        BigDecimal amount,
        String selection,
        String status,
        OffsetDateTime placedAt
) {
}

