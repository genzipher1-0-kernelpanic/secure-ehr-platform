package com.ehrplatform.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Response DTO for audit event queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant occurredAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant receivedAt;

    private String sourceService;
    private String sourceInstance;

    private String eventType;
    private String outcome;
    private String severity;

    private Long actorUserId;
    private String actorRole;
    private String actorEmail;

    private String ip;
    private String userAgent;
    private String deviceId;
    private String sessionId;

    private Long patientId;
    private Long recordId;
    private Long targetUserId;

    private String requestId;
    private String traceId;
    private String spanId;

    private Map<String, Object> details;

    private String prevHash;
    private String eventHash;
}
