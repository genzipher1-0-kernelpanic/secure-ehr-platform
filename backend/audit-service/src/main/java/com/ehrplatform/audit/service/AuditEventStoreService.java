package com.ehrplatform.audit.service;

import com.ehrplatform.audit.dto.AuditEventMessage;
import com.ehrplatform.audit.dto.AuditEventResponse;
import com.ehrplatform.audit.dto.AuditQueryRequest;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.repository.AuditEventRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for storing and querying audit events with hash chain integrity.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditEventStoreService {

    private final AuditEventRepository auditEventRepository;
    private final HashChainService hashChainService;

    /**
     * Store a new audit event with hash chain.
     * Handles idempotency via request_id unique constraint.
     * Normalizes minimal events from care-service by filling defaults.
     *
     * @param message The audit event message from Kafka
     * @return The stored audit event, or existing one if duplicate
     */
    @Transactional
    public AuditEvent storeEvent(AuditEventMessage message) {
        // Normalize the message - generate requestId if missing
        String effectiveRequestId = message.getEffectiveRequestId();
        
        // Idempotency check
        Optional<AuditEvent> existing = auditEventRepository.findByRequestId(effectiveRequestId);
        if (existing.isPresent()) {
            log.info("Duplicate event detected, requestId={}", effectiveRequestId);
            return existing.get();
        }

        // Get previous hash for chain
        String prevHash = auditEventRepository.findLatestEventHash()
                .orElse(hashChainService.getGenesisHash());

        // Build entity with normalized/default values for missing fields
        AuditEvent event = AuditEvent.builder()
                .occurredAt(message.getEffectiveOccurredAt())
                .receivedAt(Instant.now())
                .sourceService(message.getEffectiveSourceService())
                .sourceInstance(message.getSourceInstance())
                .eventType(message.getEventType())
                .outcome(message.getEffectiveOutcome())
                .severity(message.getEffectiveSeverity())
                .actorUserId(message.getEffectiveActorUserId())
                .actorRole(message.getEffectiveActorRole())
                .actorEmail(message.getActorEmail())
                .ip(message.getIp())
                .userAgent(message.getUserAgent())
                .deviceId(message.getDeviceId())
                .sessionId(message.getSessionId())
                .patientId(message.getPatientId())
                .recordId(message.getRecordId())
                .targetUserId(message.getTargetUserId())
                .requestId(effectiveRequestId)
                .traceId(message.getTraceId())
                .spanId(message.getSpanId())
                .detailsJson(message.getDetails())
                .prevHash(prevHash)
                .build();

        // Compute hash for this event
        String eventHash = hashChainService.computeEventHash(prevHash, event);
        event.setEventHash(eventHash);

        // Save
        AuditEvent saved = auditEventRepository.save(event);
        log.debug("Stored audit event: id={}, requestId={}, eventType={}", 
                saved.getId(), saved.getRequestId(), saved.getEventType());

        return saved;
    }

    /**
     * Get audit event by ID
     */
    public Optional<AuditEvent> findById(Long id) {
        return auditEventRepository.findById(id);
    }

    /**
     * Query audit events with filters and pagination
     */
    public Page<AuditEvent> queryEvents(AuditQueryRequest request) {
        Specification<AuditEvent> spec = buildSpecification(request);
        
        Sort sort = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        return auditEventRepository.findAll(spec, pageable);
    }

    /**
     * Build JPA Specification from query request
     */
    private Specification<AuditEvent> buildSpecification(AuditQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), request.getToDate()));
            }
            if (request.getSourceService() != null) {
                predicates.add(cb.equal(root.get("sourceService"), request.getSourceService()));
            }
            if (request.getEventType() != null) {
                predicates.add(cb.equal(root.get("eventType"), request.getEventType()));
            }
            if (request.getOutcome() != null) {
                predicates.add(cb.equal(root.get("outcome"), request.getOutcome()));
            }
            if (request.getSeverity() != null) {
                predicates.add(cb.equal(root.get("severity"), request.getSeverity()));
            }
            if (request.getActorUserId() != null) {
                predicates.add(cb.equal(root.get("actorUserId"), request.getActorUserId()));
            }
            if (request.getActorEmail() != null) {
                predicates.add(cb.equal(root.get("actorEmail"), request.getActorEmail()));
            }
            if (request.getActorRole() != null) {
                predicates.add(cb.equal(root.get("actorRole"), request.getActorRole()));
            }
            if (request.getPatientId() != null) {
                predicates.add(cb.equal(root.get("patientId"), request.getPatientId()));
            }
            if (request.getRecordId() != null) {
                predicates.add(cb.equal(root.get("recordId"), request.getRecordId()));
            }
            if (request.getTargetUserId() != null) {
                predicates.add(cb.equal(root.get("targetUserId"), request.getTargetUserId()));
            }
            if (request.getIp() != null) {
                predicates.add(cb.equal(root.get("ip"), request.getIp()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Convert entity to response DTO
     */
    public AuditEventResponse toResponse(AuditEvent event) {
        return AuditEventResponse.builder()
                .id(event.getId())
                .occurredAt(event.getOccurredAt())
                .receivedAt(event.getReceivedAt())
                .sourceService(event.getSourceService())
                .sourceInstance(event.getSourceInstance())
                .eventType(event.getEventType())
                .outcome(event.getOutcome())
                .severity(event.getSeverity())
                .actorUserId(event.getActorUserId())
                .actorRole(event.getActorRole())
                .actorEmail(event.getActorEmail())
                .ip(event.getIp())
                .userAgent(event.getUserAgent())
                .deviceId(event.getDeviceId())
                .sessionId(event.getSessionId())
                .patientId(event.getPatientId())
                .recordId(event.getRecordId())
                .targetUserId(event.getTargetUserId())
                .requestId(event.getRequestId())
                .traceId(event.getTraceId())
                .spanId(event.getSpanId())
                .details(event.getDetailsJson())
                .prevHash(event.getPrevHash())
                .eventHash(event.getEventHash())
                .build();
    }
}
