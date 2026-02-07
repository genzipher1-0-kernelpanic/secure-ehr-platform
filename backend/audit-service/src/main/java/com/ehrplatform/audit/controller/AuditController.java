package com.ehrplatform.audit.controller;

import com.ehrplatform.audit.dto.*;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.service.AuditEventStoreService;
import com.ehrplatform.audit.service.IntegrityVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST controller for admin audit operations.
 * All endpoints require ADMIN, SYS_ADMIN, or SUPER_ADMIN role.
 */
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('SYS_ADMIN', 'SUPER_ADMIN', 'ADMIN0', 'ADMIN1')")
public class AuditController {

    private final AuditEventStoreService auditEventStoreService;
    private final IntegrityVerificationService integrityVerificationService;

    /**
     * Query audit events with filters and pagination.
     * 
     * GET /admin/audit/events?fromDate=...&toDate=...&eventType=...&page=0&size=50
     */
    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventResponse>> queryEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) String sourceService,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String actorRole,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long recordId,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) String ip,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "occurredAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        AuditQueryRequest request = AuditQueryRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .sourceService(sourceService)
                .eventType(eventType)
                .outcome(outcome)
                .severity(severity)
                .actorUserId(actorUserId)
                .actorEmail(actorEmail)
                .actorRole(actorRole)
                .patientId(patientId)
                .recordId(recordId)
                .targetUserId(targetUserId)
                .ip(ip)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<AuditEvent> events = auditEventStoreService.queryEvents(request);
        Page<AuditEventResponse> response = events.map(auditEventStoreService::toResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single audit event by ID.
     * 
     * GET /admin/audit/events/{id}
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<AuditEventResponse> getEvent(@PathVariable Long id) {
        return auditEventStoreService.findById(id)
                .map(auditEventStoreService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verify the integrity of the audit hash chain.
     * 
     * GET /admin/audit/verify?fromId=1&toId=1000
     */
    @GetMapping("/verify")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN0')")
    public ResponseEntity<IntegrityVerifyResponse> verifyIntegrity(
            @RequestParam Long fromId,
            @RequestParam Long toId
    ) {
        if (fromId > toId) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Starting integrity verification: fromId={}, toId={}", fromId, toId);
        IntegrityVerifyResponse result = integrityVerificationService.verifyRange(fromId, toId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get event types summary.
     * 
     * GET /admin/audit/event-types
     */
    @GetMapping("/event-types")
    public ResponseEntity<String[]> getEventTypes() {
        String[] eventTypes = {
                "LOGIN_SUCCESS",
                "LOGIN_FAILURE",
                "LOGOUT",
                "TOKEN_REFRESH",
                "PASSWORD_CHANGED",
                "PASSWORD_RESET_REQUESTED",
                "MFA_ENABLED",
                "MFA_DISABLED",
                "RECORD_VIEWED",
                "RECORD_CREATED",
                "RECORD_UPDATED",
                "RECORD_DELETED",
                "PATIENT_ACCESSED",
                "EXPORT_REQUESTED",
                "ROLE_CHANGED",
                "PERMISSION_CHANGED",
                "USER_CREATED",
                "USER_DELETED",
                "ADMIN_CREATED",
                "ADMIN_DELETED",
                "RETENTION_CHANGED",
                "ACCESS_DENIED"
        };
        return ResponseEntity.ok(eventTypes);
    }
}
