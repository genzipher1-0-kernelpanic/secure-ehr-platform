package com.genzipher.identityservice.Model;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tokens",
        indexes = {
                @Index(name = "idx_tokens_user", columnList = "user_id"),
                @Index(name = "idx_tokens_access_exp", columnList = "access_expires_at"),
                @Index(name = "idx_tokens_refresh_exp", columnList = "refresh_expires_at")
        }
)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "access_token_hash", nullable = false, columnDefinition = "VARBINARY(64)")
    private byte[] accessTokenHash;

    @Column(name = "refresh_token_hash", nullable = false, columnDefinition = "VARBINARY(64)")
    private byte[] refreshTokenHash;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "access_expires_at", nullable = false)
    private Instant accessExpiresAt;

    @Column(name = "refresh_expires_at", nullable = false)
    private Instant refreshExpiresAt;

    @Column(name = "access_revoked_at")
    private Instant accessRevokedAt;

    @Column(name = "refresh_revoked_at")
    private Instant refreshRevokedAt;

    @PrePersist
    void onCreate() {
        this.issuedAt = Instant.now();
    }

    public boolean isAccessRevokedOrExpired() {
        return accessRevokedAt != null || accessExpiresAt.isBefore(Instant.now());
    }

    public boolean isRefreshRevokedOrExpired() {
        return refreshRevokedAt != null || refreshExpiresAt.isBefore(Instant.now());
    }
}
