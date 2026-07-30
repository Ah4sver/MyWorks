package com.daniilkhanukov.spring.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // тот же секрет, что и в пропертиз
    private static final String TEST_SECRET =
            "dGVzdC10YXNrLXN1cGVyLXNlY3JldC1rZXktZm9yLWp3dC1zaWduaW5nLW1pbi0yNTYtYml0cw==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 30);
    }

    @Test
    void generateToken_shouldProduceValidToken() {
        String token = jwtService.generateToken("user@example.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void extractEmail_shouldReturnOriginalSubject() {
        String token = jwtService.generateToken("user@example.com");

        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        assertThat(jwtService.isTokenValid("not.a.valid.jwt.token")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsEmpty() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsSignedWithDifferentKey() {
        String diffSecret = "YW5vdGhlci1kaWZmZXJlbnQtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcw==";
        JwtService otherJwtService = new JwtService(diffSecret, 30);

        String tokenFromOtherService = otherJwtService.generateToken("user@example.com");

        assertThat(jwtService.isTokenValid(tokenFromOtherService)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, 0);
        String token = shortLivedJwtService.generateToken("user@example.com");

        Thread.sleep(50);

        assertThat(shortLivedJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void getExpirationSeconds_shouldConvertMinutesToSeconds() {
        JwtService service = new JwtService(TEST_SECRET, 5);

        assertThat(service.getExpirationSeconds()).isEqualTo(300L);
    }
}