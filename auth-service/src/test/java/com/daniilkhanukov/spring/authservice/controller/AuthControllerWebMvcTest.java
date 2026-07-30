package com.daniilkhanukov.spring.authservice.controller;

import com.daniilkhanukov.spring.authservice.dto.RegisterRequest;
import com.daniilkhanukov.spring.authservice.dto.RegisterResponse;
import com.daniilkhanukov.spring.authservice.dto.TokenResponse;
import com.daniilkhanukov.spring.authservice.dto.VerifyRequest;
import com.daniilkhanukov.spring.authservice.exception.InvalidVerificationCodeException;
import com.daniilkhanukov.spring.authservice.security.JwtAuthenticationFilter;
import com.daniilkhanukov.spring.authservice.security.JwtService;
import com.daniilkhanukov.spring.authservice.security.SecurityConfig;
import com.daniilkhanukov.spring.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Изолированный тест HTTP-слоя. Проверяю маршрутизацию, сериализацию,
 * коды ответов и работу Security-фильтра
 */
@WebMvcTest(AuthController.class)
// Без импорта не работало
@Import({JwtService.class, SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    void register_shouldReturn200_withValidEmail() throws Exception {
        when(authService.registerOrRequestCode("user@example.com"))
                .thenReturn(new RegisterResponse("Код подтверждения отправлен на почту", "user@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("user@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void register_shouldReturn400_withInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("not-valid"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_shouldReturn400_withBlankEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_shouldReturn200_andToken_whenCodeIsValid() throws Exception {
        when(authService.verifyCode("user@example.com", "123456"))
                .thenReturn(TokenResponse.bearer("some.jwt.token", 1800L));

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyRequest("user@example.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("some.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void verify_shouldReturn400_whenCodeIsInvalid() throws Exception {
        when(authService.verifyCode(anyString(), anyString()))
                .thenThrow(new InvalidVerificationCodeException("Неверный код подтверждения"));

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyRequest("user@example.com", "000000"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_VERIFICATION_CODE"));
    }

    @Test
    void protectedEndpoint_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturn200_withValidToken() throws Exception {
        String token = jwtService.generateToken("secured-user@example.com");

        mockMvc.perform(get("/api/auth/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("secured-user@example.com")));
    }

    @Test
    void protectedEndpoint_shouldReturn401_withExpiredToken() throws Exception {
        JwtService shortLivedService = new JwtService(
                "dGVzdC10YXNrLXN1cGVyLXNlY3JldC1rZXktZm9yLWp3dC1zaWduaW5nLW1pbi0yNTYtYml0cw==", 0);
        String expiredToken = shortLivedService.generateToken("user@example.com");
        Thread.sleep(50);

        mockMvc.perform(get("/api/auth/protected")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
