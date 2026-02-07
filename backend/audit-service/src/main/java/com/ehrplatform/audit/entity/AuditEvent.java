package com.ehrplatform.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for audit_event table.
 * Represents a tamper-evident audit log entry.
 */
@Entity
@Table(name = "audit_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    // Source
    @Column(name = "source_service", nullable = false, length = 64)
    private String sourceService;

    @Column(name = "source_instance", length = 128)
    private String sourceInstance;

    // Event classification
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "outcome", nullable = false, length = 16)
    private String outcome;

    @Column(name = "severity", nullable = false, length = 16)
    @Builder.Default
    private String severity = "INFO";

    // Identity/context
    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role", length = 32)
    private String actorRole;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    // Domain references
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    // Request correlation
    @Column(name = "request_id", length = 64, unique = true)
    private String requestId;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "span_id", length = 64)
    private String spanId;

    // Extra metadata (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "JSON")
    private Map<String, Object> detailsJson;

    // Tamper-evident chain
    @Column(name = "prev_hash", nullable = false, length = 64)
    private String prevHash;

    @Column(name = "event_hash", nullable = false, length = 64)
    private String eventHash;
}
