package com.mondial.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public record CreateGameRequest(
        @NotBlank String name,
        @Future OffsetDateTime startsAt
) {
}

