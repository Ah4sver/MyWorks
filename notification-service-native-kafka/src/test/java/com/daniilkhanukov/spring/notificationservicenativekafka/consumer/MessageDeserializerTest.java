package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageDeserializerTest {

    private final MessageDeserializer deserializer = new MessageDeserializer();

    @Test
    void deserialize_shouldParseValidJson() {
        String json = """
                {
                  "email": "user@example.com",
                  "code": "123456",
                  "createdAt": "2026-07-15T10:00:00Z",
                  "expiresAt": "2026-07-15T10:05:00Z"
                }
                """;

        VerificationCodeMessage message = deserializer.deserialize(json);

        assertThat(message.email()).isEqualTo("user@example.com");
        assertThat(message.code()).isEqualTo("123456");
        assertThat(message.createdAt()).isEqualTo(Instant.parse("2026-07-15T10:00:00Z"));
        assertThat(message.expiresAt()).isEqualTo(Instant.parse("2026-07-15T10:05:00Z"));
    }

    @Test
    void deserialize_shouldThrow_onMalformedJson() {
        String malformedJson = "{ this is not valid json ";

        assertThatThrownBy(() -> deserializer.deserialize(malformedJson))
                .isInstanceOf(MessageDeserializationException.class);
    }

    @Test
    void deserialize_shouldIgnoreUnknownFields() {
        String jsonWithExtraField = """
                {
                  "email": "user@example.com",
                  "code": "123456",
                  "createdAt": "2026-07-15T10:00:00Z",
                  "expiresAt": "2026-07-15T10:05:00Z",
                  "someFutureField": "should be ignored"
                }
                """;

        VerificationCodeMessage message = deserializer.deserialize(jsonWithExtraField);

        assertThat(message.email()).isEqualTo("user@example.com");
    }
}
