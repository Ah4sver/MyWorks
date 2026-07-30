package com.daniilkhanukov.spring.authservice.service;

import com.daniilkhanukov.spring.authservice.dto.RegisterResponse;
import com.daniilkhanukov.spring.authservice.dto.TokenResponse;
import com.daniilkhanukov.spring.authservice.entity.User;
import com.daniilkhanukov.spring.authservice.entity.VerificationCode;
import com.daniilkhanukov.spring.authservice.exception.InvalidVerificationCodeException;
import com.daniilkhanukov.spring.authservice.kafka.VerificationCodeProducer;
import com.daniilkhanukov.spring.authservice.repository.UserRepository;
import com.daniilkhanukov.spring.authservice.repository.VerificationCodeRepository;
import com.daniilkhanukov.spring.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private VerificationCodeProducer verificationCodeProducer;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, verificationCodeRepository,
                verificationCodeProducer, jwtService);
        ReflectionTestUtils.setField(authService, "codeTtlMinutes", 5L);
        ReflectionTestUtils.setField(authService, "codeLength", 6);
    }

    @Test
    void registerOrRequestCode_shouldCreateNewUser_whenUserDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.registerOrRequestCode(EMAIL);

        assertThat(response.email()).isEqualTo(EMAIL);
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationCodeProducer, times(1)).sendVerificationCode(any());
    }

    @Test
    void registerOrRequestCode_shouldReuseExistingUser_whenUserAlreadyExists() {
        User existingUser = User.builder().id(1L).email(EMAIL).createdAt(Instant.now()).build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.registerOrRequestCode(EMAIL);

        assertThat(response.email()).isEqualTo(EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verify(verificationCodeProducer, times(1)).sendVerificationCode(any());
    }

    @Test
    void registerOrRequestCode_shouldSendCodeOfConfiguredLength() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerOrRequestCode(EMAIL);

        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeRepository).save(captor.capture());

        assertThat(captor.getValue().getCode()).hasSize(6);
        assertThat(captor.getValue().getCode()).matches("\\d{6}");
    }

    @Test
    void verifyCode_shouldReturnToken_whenCodeIsCorrect() {
        VerificationCode validCode = VerificationCode.builder()
                .id(1L)
                .email(EMAIL)
                .code("123456")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.of(validCode));
        when(jwtService.generateToken(EMAIL)).thenReturn("fake.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(1800L);

        TokenResponse response = authService.verifyCode(EMAIL, "123456");

        assertThat(response.accessToken()).isEqualTo("fake.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(1800L);
        assertThat(validCode.isUsed()).isTrue();
    }

    @Test
    void verifyCode_shouldThrow_whenCodeDoesNotExist() {
        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyCode(EMAIL, "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessageContaining("не запрашивался");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void verifyCode_shouldThrow_whenCodeIsIncorrect() {
        VerificationCode validCode = VerificationCode.builder()
                .id(1L)
                .email(EMAIL)
                .code("123456")
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.of(validCode));

        assertThatThrownBy(() -> authService.verifyCode(EMAIL, "999999"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessageContaining("Неверный код");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void verifyCode_shouldThrow_whenCodeIsExpired() {
        VerificationCode expiredCode = VerificationCode.builder()
                .id(1L)
                .email(EMAIL)
                .code("123456")
                .createdAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.of(expiredCode));

        assertThatThrownBy(() -> authService.verifyCode(EMAIL, "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessageContaining("истёк");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void verifyCode_shouldNotFindUsedCode_becauseRepositoryFiltersUsedFalse() {
        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyCode(EMAIL, "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }
}
