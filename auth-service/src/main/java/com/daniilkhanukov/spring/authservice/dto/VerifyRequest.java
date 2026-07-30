package com.daniilkhanukov.spring.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(

        @NotBlank(message = "email не должен быть пустым")
        @Email(message = "некорректный формат email")
        String email,

        @NotBlank(message = "код подтверждения не должен быть пустым")
        String code
) {
}
