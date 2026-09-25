package com.daniilkhanukov.spring.myworks.dto;

import java.time.OffsetDateTime;

public record ShortenResponse(
        String shortUrl,
        String shortCode,
        String originalUrl,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {
}
