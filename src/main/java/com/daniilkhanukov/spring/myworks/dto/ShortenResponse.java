package com.daniilkhanukov.spring.myworks.dto;

import java.time.OffsetDateTime;

public record ShortenResponse(
        String shortUrl,
        String shortLink,
        String longLink,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
) {
}
