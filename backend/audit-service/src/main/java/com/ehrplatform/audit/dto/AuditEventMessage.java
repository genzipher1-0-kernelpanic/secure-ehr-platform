package com.ehrplatform.audit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Flexible DTO for audit events from producers (care-service, gateway, identity, ehr).
 * Accepts both minimal events (from care-service) and full events.
 * 
 * Minimal format from care-service:
 * {
 *   "eventType": "ASSIGNMENT_CREATED",
 *   "patientId": 7,
 *   "doctorUserId": 34,
 *   "role": "DOCTOR",
 *   "occurredAt": "2026-02-07T23:20:00Z"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventMessage {

    // ========== Core Fields (from care-service) ==========

    /**
     * Event type classification (e.g., "ASSIGNMENT_CREATED", "LOGIN_SUCCESS")
     */
    private String eventType;

    /**
     * When the event occurred (UTC)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant occurredAt;

    /**
     * Patient ID (for healthcare domain events)
     */
    private Long patientId;

    /**
     * Doctor user ID (from care-service events)
     */
    @JsonAlias({"doctorUserId", "actorUserId"})
    private Long doctorUserId;

    /**
     * Role associated with the event
     */
    @JsonAlias({"role", "actorRole"})
    private String role;

    // ========== Optional Extended Fields ==========

    /**
     * Source service name (e.g., "gateway", "identity", "ehr", "care")
     * Will default to "unknown" if not provided
     */
    private String sourceService;

    /**
     * Source instance/hostname
     */
    private String sourceInstance;

    /**
     * Outcome of the event: SUCCESS, FAILURE, or DENIED
     * Will default to "SUCCESS" if not provided
     */
    private String outcome;

    /**
     * Severity level: INFO, WARN, HIGH, or CRITICAL
     * Will default to "INFO" if not provided
     */
    @Builder.Default
    private String severity = "INFO";

    /**
     * Unique request ID for idempotency (UUID recommended)
     * Will be auto-generated if not provided
     */
    private String requestId;

    /**
     * Actor's user ID (mapped from doctorUserId if not set)
     */
    private Long actorUserId;

    /**
     * Actor's role (mapped from role if not set)
     */
    private String actorRole;

    /**
     * Actor's email
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
     * Medical record ID
     */
    private Long recordId;

    /**
     * Target user ID (for admin actions like role changes)
     */
    private Long targetUserId;

    /**
     * Trace ID for distributed tracing
     */
    private String traceId;

    /**
     * Span ID for distributed tracing
     */
    private String spanId;

    /**
     * Additional metadata
     */
    private Map<String, Object> details;

    // ========== Helper Methods for Normalization ==========

    /**
     * Get effective actor user ID (prefers actorUserId, falls back to doctorUserId)
     */
    public Long getEffectiveActorUserId() {
        if (actorUserId != null) {
            return actorUserId;
        }
        return doctorUserId;
    }

    /**
     * Get effective actor role (prefers actorRole, falls back to role)
     */
    public String getEffectiveActorRole() {
        if (actorRole != null && !actorRole.isEmpty()) {
            return actorRole;
        }
        return role;
    }

    /**
     * Get effective request ID (auto-generates if null)
     */
    public String getEffectiveRequestId() {
        if (requestId != null && !requestId.isEmpty()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Get effective source service (defaults to "care-service" if null)
     */
    public String getEffectiveSourceService() {
        if (sourceService != null && !sourceService.isEmpty()) {
            return sourceService;
        }
        return "care-service";
    }

    /**
     * Get effective outcome (defaults to "SUCCESS" if null)
     */
    public String getEffectiveOutcome() {
        if (outcome != null && !outcome.isEmpty()) {
            return outcome;
        }
        return "SUCCESS";
    }

    /**
     * Get effective severity (defaults to "INFO" if null)
     */
    public String getEffectiveSeverity() {
        if (severity != null && !severity.isEmpty()) {
            return severity;
        }
        return "INFO";
    }

    /**
     * Get effective occurred at (defaults to now if null)
     */
    public Instant getEffectiveOccurredAt() {
        if (occurredAt != null) {
            return occurredAt;
        }
        return Instant.now();
    }
}
