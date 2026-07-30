package com.daniilkhanukov.spring.authservice.dto;

public record TokenResponse(

        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
    public static TokenResponse bearer(String token, long expiresInSeconds) {
        return new TokenResponse(token, "Bearer", expiresInSeconds);
    }
}
