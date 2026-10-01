package com.daniilkhanukov.spring.myworks.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.OffsetDateTime;

public record ShortenRequest(

        @NotBlank(message = "url must not be blank")
        @Size(max = 2048, message = "url must not exceed 2048 characters")
        @URL(
                regexp = "^https?://.*",
                message = "url must be a valid http/https URL"
        )
        String url,

        @Size(min = 3, max = 32, message = "alias length must be between 3 and 32")
        @Pattern(
                regexp = "^[a-zA-Z0-9_-]*$",
                message = "alias may contain only letters, digits, '-' and '_'"
        )
        String alias,

        @Future(message = "expiresAt must be in future")
        OffsetDateTime expiresAt
){
}
