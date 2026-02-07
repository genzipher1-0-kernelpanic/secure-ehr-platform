package com.ehrplatform.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Query parameters for audit event search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditQueryRequest {

    private Instant fromDate;
    private Instant toDate;

    private String sourceService;
    private String eventType;
    private String outcome;
    private String severity;

    private Long actorUserId;
    private String actorEmail;
    private String actorRole;

    private Long patientId;
    private Long recordId;
    private Long targetUserId;

    private String ip;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 50;

    @Builder.Default
    private String sortBy = "occurredAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
