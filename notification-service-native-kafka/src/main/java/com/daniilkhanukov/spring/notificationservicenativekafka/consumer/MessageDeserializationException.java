package com.daniilkhanukov.spring.notificationservicenativekafka.consumer;

public class MessageDeserializationException extends RuntimeException {
    public MessageDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
