package com.ehrplatform.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for publishing alerts to Kafka.
 * Consumed by notification-service to send notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {

    /**
     * Alert ID from database
     */
    private Long alertId;

    /**
     * When the alert was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant createdAt;

    /**
     * Alert type: ADMIN_POLICY_CHANGE, REPEATED_FAILED_LOGIN, 
     * DENIED_ACCESS_BURST, EXPORT_SPIKE, BULK_ACCESS, AUDIT_CHAIN_FAIL
     */
    private String alertType;

    /**
     * Severity level: INFO, MED, HIGH, CRITICAL
     */
    private String severity;

    /**
     * Alert status: OPEN, ACKED, RESOLVED
     */
    private String status;

    /**
     * Short title for the alert
     */
    private String title;

    /**
     * Detailed message describing the alert
     */
    private String message;

    /**
     * User who triggered the alert (if applicable)
     */
    private Long actorUserId;

    /**
     * Email of the user who triggered the alert
     */
    private String actorEmail;

    /**
     * IP address associated with the alert
     */
    private String ip;

    /**
     * Patient ID (if applicable)
     */
    private Long patientId;

    /**
     * Additional evidence/metadata
     */
    private Map<String, Object> evidence;

    /**
     * Create AlertMessage from Alert entity
     */
    public static AlertMessage fromAlert(com.ehrplatform.audit.entity.Alert alert) {
        return AlertMessage.builder()
                .alertId(alert.getId())
                .createdAt(alert.getCreatedAt())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .actorUserId(alert.getActorUserId())
                .actorEmail(alert.getActorEmail())
                .ip(alert.getIp())
                .patientId(alert.getPatientId())
                .evidence(alert.getEvidenceJson())
                .build();
    }
}
