package com.daniilkhanukov.spring.authservice.repository;

import com.daniilkhanukov.spring.authservice.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Если пользователь запросил код несколько раз подряд, нужно выдать самый свежий неиспользованный код
     */
    Optional<VerificationCode> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    /**
     * Метод для очистки старых кодов
     */
    @Modifying
    @Query("delete from VerificationCode v where v.expiresAt < :now")
    int deleteAllExpired(@Param("now") Instant now);
}