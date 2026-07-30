package com.daniilkhanukov.spring.authservice.controller;

import com.daniilkhanukov.spring.authservice.dto.RegisterRequest;
import com.daniilkhanukov.spring.authservice.dto.RegisterResponse;
import com.daniilkhanukov.spring.authservice.dto.TokenResponse;
import com.daniilkhanukov.spring.authservice.dto.VerifyRequest;
import com.daniilkhanukov.spring.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.registerOrRequestCode(request.email());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Пользователь вводит полученный подтверждения.
     * Если код верный - возвращается access token
     */
    @PostMapping("/verify")
    public ResponseEntity<TokenResponse> verify(@Valid @RequestBody VerifyRequest request) {
        TokenResponse response = authService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok(response);
    }

    /**
     * Эндпоинт, который можно вызвать только с валидным access token
     */
    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(java.security.Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok("Привет, " + email + "! Это защищённый эндпоинт, ты справился и получил доступ по валидному токену, молодец!");
    }
}
