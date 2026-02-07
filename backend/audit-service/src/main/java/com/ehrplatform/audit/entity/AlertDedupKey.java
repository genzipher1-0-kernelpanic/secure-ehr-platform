package com.ehrplatform.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for alert_dedup_key table.
 * Prevents duplicate alerts within a time window.
 */
@Entity
@Table(name = "alert_dedup_key")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDedupKey {

    @Id
    @Column(name = "dedup_key", length = 128)
    private String dedupKey;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", insertable = false, updatable = false)
    private Alert alert;
}
