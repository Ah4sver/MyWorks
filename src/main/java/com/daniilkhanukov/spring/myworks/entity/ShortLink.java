package com.daniilkhanukov.spring.myworks.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Clock;
import java.time.OffsetDateTime;

@Entity
@Table(name = "short_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_link", nullable = false, length = 64, unique = true)
    private String shortLink;

    @Column(name = "long_link", nullable = false, columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String longLink;

    @Column(name = "alias", nullable = false)
    private boolean alias;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public boolean isExpired(Clock clock) {
        return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now(clock));
    }

}
