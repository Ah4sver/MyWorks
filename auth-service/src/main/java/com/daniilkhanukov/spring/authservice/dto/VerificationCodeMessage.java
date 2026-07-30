package com.daniilkhanukov.spring.authservice.dto;

import java.time.Instant;

public record VerificationCodeMessage(
        String email,
        String code,
        Instant createdAt,
        Instant expiresAt
) {
}
