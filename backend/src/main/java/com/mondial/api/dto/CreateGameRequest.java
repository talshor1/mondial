package com.mondial.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record CreateGameRequest(
        @NotBlank String homeTeam,
        @NotBlank String awayTeam,
        OffsetDateTime startsAt
) {}