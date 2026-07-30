package com.daniilkhanukov.spring.authservice.integration;

import com.daniilkhanukov.spring.authservice.dto.RegisterRequest;
import com.daniilkhanukov.spring.authservice.dto.TokenResponse;
import com.daniilkhanukov.spring.authservice.dto.VerifyRequest;
import com.daniilkhanukov.spring.authservice.entity.VerificationCode;
import com.daniilkhanukov.spring.authservice.repository.VerificationCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"verification-codes-test"})
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void fullRegistrationAndVerificationFlow_shouldSucceed() {
        String email = "integration-test-" + System.nanoTime() + "@example.com";

        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                url("/api/auth/register"),
                new RegisterRequest(email),
                String.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional<VerificationCode> savedCode = verificationCodeRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email);
        assertThat(savedCode).isPresent();
        String actualCode = savedCode.get().getCode();

        ResponseEntity<String> wrongCodeResponse = restTemplate.postForEntity(
                url("/api/auth/verify"),
                new VerifyRequest(email, "000000".equals(actualCode) ? "111111" : "000000"),
                String.class
        );
        assertThat(wrongCodeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<TokenResponse> verifyResponse = restTemplate.postForEntity(
                url("/api/auth/verify"),
                new VerifyRequest(email, actualCode),
                TokenResponse.class
        );
        assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyResponse.getBody()).isNotNull();
        String accessToken = verifyResponse.getBody().accessToken();
        assertThat(accessToken).isNotBlank();

        ResponseEntity<String> unauthorizedResponse = restTemplate.getForEntity(
                url("/api/auth/protected"), String.class);
        assertThat(unauthorizedResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> authorizedRequest = new HttpEntity<>(headers);

        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                url("/api/auth/protected"), HttpMethod.GET, authorizedRequest, String.class);
        assertThat(protectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(protectedResponse.getBody()).contains(email);

        ResponseEntity<String> reusedCodeResponse = restTemplate.postForEntity(
                url("/api/auth/verify"),
                new VerifyRequest(email, actualCode),
                String.class
        );
        assertThat(reusedCodeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void protectedEndpoint_shouldReturn401_withMalformedToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("this-is-not-a-valid-jwt");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/auth/protected"), HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void verify_shouldReturn400_whenCodeWasNeverRequested() {
        String email = "never-registered-" + System.nanoTime() + "@example.com";

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/auth/verify"),
                new VerifyRequest(email, "123456"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/api/auth/register"),
                new RegisterRequest("not-an-email"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
