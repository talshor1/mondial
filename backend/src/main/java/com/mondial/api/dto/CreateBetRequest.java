package com.mondial.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateBetRequest(
        @NotNull Long gameId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String selection
) {
}

