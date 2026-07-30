package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

import com.daniilkhanukov.spring.notificationservicenativekafka.dto.VerificationCodeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Ручная JSON-десериализация сообщений из Kafka, т.к. тут нет спрингового JsonDeserializer
 */
public class MessageDeserializer {

    private final ObjectMapper objectMapper;

    public MessageDeserializer() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public VerificationCodeMessage deserialize(String json) {
        try {
            return objectMapper.readValue(json, VerificationCodeMessage.class);
        } catch (Exception e) {
            throw new MessageDeserializationException("Не удалось распарсить сообщение из Kafka: " + json, e);
        }
    }
}
