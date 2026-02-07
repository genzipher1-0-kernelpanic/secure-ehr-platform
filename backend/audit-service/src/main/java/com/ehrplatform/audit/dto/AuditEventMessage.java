package com.ehrplatform.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Shared DTO contract for audit events from all producers (gateway/identity/ehr).
 * This is the Kafka message format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventMessage {

    // ========== Required Fields ==========

    /**
     * When the event occurred (UTC)
     */
    @NotNull(message = "occurredAt is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant occurredAt;

    /**
     * Source service name (e.g., "gateway", "identity", "ehr", "care")
     */
    @NotBlank(message = "sourceService is required")
    private String sourceService;

    /**
     * Event type classification (e.g., "LOGIN_SUCCESS", "LOGIN_FAILURE", "RECORD_VIEWED")
     */
    @NotBlank(message = "eventType is required")
    private String eventType;

    /**
     * Outcome of the event: SUCCESS, FAILURE, or DENIED
     */
    @NotBlank(message = "outcome is required")
    private String outcome;

    /**
     * Severity level: INFO, WARN, HIGH, or CRITICAL
     */
    @NotBlank(message = "severity is required")
    @Builder.Default
    private String severity = "INFO";

    /**
     * Unique request ID for idempotency (UUID recommended)
     */
    @NotBlank(message = "requestId is required")
    private String requestId;

    // ========== Optional Fields ==========

    /**
     * Optional: source instance/hostname
     */
    private String sourceInstance;

    /**
     * Actor's user ID (null for pre-auth events like login failures)
     */
    private Long actorUserId;

    /**
     * Actor's role (PATIENT, DOCTOR, ADMIN, SYS_ADMIN, SUPER_ADMIN)
     */
    private String actorRole;

    /**
     * Actor's email (useful for login failures where no userId yet)
     */
    private String actorEmail;

    /**
     * Client IP address
     */
    private String ip;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * Optional device identifier
     */
    private String deviceId;

    /**
     * Optional session ID
     */
    private String sessionId;

    /**
     * Patient ID (for healthcare domain events)
     */
    private Long patientId;

    /**
     * Medical record ID
     */
    private Long recordId;

    /**
     * Target user ID (for admin actions like role changes)
     */
    private Long targetUserId;

    /**
     * Trace ID for distributed tracing (Zipkin/Jaeger)
     */
    private String traceId;

    /**
     * Span ID for distributed tracing
     */
    private String spanId;

    /**
     * Additional metadata (sanitized, never PHI text)
     */
    private Map<String, Object> details;
}
