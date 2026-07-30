package com.daniilkhanukov.spring.notificationservicespringkafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Узнал, что есть смысл добавлять @JsonIgnoreProperties на случай, если сервис в будущем
 * начнёт слать дополнительные поля, чтобы десериализация не падала, чисто для себя добавил
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerificationCodeMessage(
        String email,
        String code,
        Instant createdAt,
        Instant expiresAt
) {
}
