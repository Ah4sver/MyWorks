package com.daniilkhanukov.spring.authservice.exception;

/**
 * Выбрасывается, если код подтверждения неверный, уже использован,
 * истёк, либо не был запрошен для данного email.
 */
public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
