package com.mondial.api.dto;

import java.time.OffsetDateTime;

public record GameResponse(
        Long id,
        String name,
        String status,
        OffsetDateTime startsAt,
        String createdBy
) {
}

