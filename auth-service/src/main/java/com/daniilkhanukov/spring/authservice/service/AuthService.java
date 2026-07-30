package com.daniilkhanukov.spring.authservice.service;

import com.daniilkhanukov.spring.authservice.dto.RegisterResponse;
import com.daniilkhanukov.spring.authservice.dto.TokenResponse;
import com.daniilkhanukov.spring.authservice.dto.VerificationCodeMessage;
import com.daniilkhanukov.spring.authservice.entity.User;
import com.daniilkhanukov.spring.authservice.entity.VerificationCode;
import com.daniilkhanukov.spring.authservice.exception.InvalidVerificationCodeException;
import com.daniilkhanukov.spring.authservice.kafka.VerificationCodeProducer;
import com.daniilkhanukov.spring.authservice.repository.UserRepository;
import com.daniilkhanukov.spring.authservice.repository.VerificationCodeRepository;
import com.daniilkhanukov.spring.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeProducer verificationCodeProducer;
    private final JwtService jwtService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.verification.code-ttl-minutes}")
    private long codeTtlMinutes;

    @Value("${app.verification.code-length}")
    private int codeLength;

    @Transactional
    public RegisterResponse registerOrRequestCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder().email(email).build();
                    User savedUser = userRepository.save(newUser);
                    log.info("Зарегистрирован новый пользовательЖ {}", email);
                    return savedUser;
                });

        String code = generateCode();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(codeTtlMinutes, ChronoUnit.MINUTES);

        VerificationCode verificationCode = VerificationCode.builder()
                .email(user.getEmail())
                .code(code)
                .createdAt(now)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        verificationCodeRepository.save(verificationCode);

        verificationCodeProducer.sendVerificationCode(
                new VerificationCodeMessage(user.getEmail(), code, now, expiresAt)
        );

        return new RegisterResponse("Код подтверждения отправлен на почту ", user.getEmail());
    }

    @Transactional
    public TokenResponse verifyCode(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidVerificationCodeException("Код подтверждения не запрашивался для email: " + email));

        if (verificationCode.isExpired()) {
            throw new InvalidVerificationCodeException("Срок действия кода истёк, запросите новый код");
        }

        if (!verificationCode.getCode().equals(code)) {
            throw new InvalidVerificationCodeException("Неверный код подтверждения");
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        String token = jwtService.generateToken(email);
        log.info("Пользователь {} успешно прошёл верификацию, выдан access token", email);

        return TokenResponse.bearer(token, jwtService.getExpirationSeconds());
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Периодическая очистка истёкших кодов подтверждения из БД
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanupExpiredCodes() {
        int deleted = verificationCodeRepository.deleteAllExpired(Instant.now());
        if (deleted > 0) {
            log.debug("Удалено {} истёкших кодов подтверждения", deleted);
        }
    }
}
