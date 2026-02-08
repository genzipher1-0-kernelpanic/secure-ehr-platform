package com.ehrplatform.audit.repository;

import com.ehrplatform.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {

    /**
     * Find by request ID (for idempotency check)
     */
    Optional<AuditEvent> findByRequestId(String requestId);

    /**
     * Check if request ID exists (faster than fetching full entity)
     */
    boolean existsByRequestId(String requestId);

    /**
     * Get the latest event hash for chain continuation
     */
    @Query("SELECT ae.eventHash FROM AuditEvent ae ORDER BY ae.id DESC LIMIT 1")
    Optional<String> findLatestEventHash();

    /**
     * Get the latest event for hash chaining
     */
    @Query("SELECT ae FROM AuditEvent ae ORDER BY ae.id DESC LIMIT 1")
    Optional<AuditEvent> findLatestEvent();

    /**
     * Find events in ID range for integrity verification
     */
    List<AuditEvent> findByIdBetweenOrderByIdAsc(Long fromId, Long toId);

    /**
     * Find event just before the given ID (for integrity verification)
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.id < :id ORDER BY ae.id DESC LIMIT 1")
    Optional<AuditEvent> findEventBeforeId(@Param("id") Long id);

    /**
     * Count login failures by email and IP within time window
     */
    @Query("SELECT ae.actorEmail, ae.ip, COUNT(ae) " +
           "FROM AuditEvent ae " +
           "WHERE ae.eventType = 'LOGIN_FAILURE' " +
           "AND ae.occurredAt >= :since " +
           "GROUP BY ae.actorEmail, ae.ip " +
           "HAVING COUNT(ae) > :threshold")
    List<Object[]> findRepeatedLoginFailures(@Param("since") Instant since, @Param("threshold") long threshold);

    /**
     * Count denied access by user and IP within time window
     */
    @Query("SELECT ae.actorUserId, ae.ip, COUNT(ae) " +
           "FROM AuditEvent ae " +
           "WHERE ae.outcome = 'DENIED' " +
           "AND ae.actorUserId IS NOT NULL " +
           "AND ae.occurredAt >= :since " +
           "GROUP BY ae.actorUserId, ae.ip " +
           "HAVING COUNT(ae) > :threshold")
    List<Object[]> findDeniedAccessBursts(@Param("since") Instant since, @Param("threshold") long threshold);

    /**
     * Count export requests within time window
     */
    @Query("SELECT ae.actorUserId, COUNT(ae) " +
           "FROM AuditEvent ae " +
           "WHERE ae.eventType = 'EXPORT_REQUESTED' " +
           "AND ae.occurredAt >= :since " +
           "GROUP BY ae.actorUserId " +
           "HAVING COUNT(ae) > :threshold")
    List<Object[]> findExportSpikes(@Param("since") Instant since, @Param("threshold") long threshold);

    /**
     * Count unique patients accessed by actor within time window
     * Includes ASSIGNMENT_CREATED events from care-service
     */
    @Query("SELECT ae.actorUserId, COUNT(DISTINCT ae.patientId) " +
           "FROM AuditEvent ae " +
           "WHERE ae.eventType IN ('RECORD_VIEWED', 'PATIENT_ACCESSED', 'ASSIGNMENT_CREATED') " +
           "AND ae.patientId IS NOT NULL " +
           "AND ae.actorUserId IS NOT NULL " +
           "AND ae.occurredAt >= :since " +
           "GROUP BY ae.actorUserId " +
           "HAVING COUNT(DISTINCT ae.patientId) > :threshold")
    List<Object[]> findBulkPatientAccess(@Param("since") Instant since, @Param("threshold") long threshold);

    /**
     * Find recent policy change events
     */
    @Query("SELECT ae FROM AuditEvent ae " +
           "WHERE ae.eventType IN ('ROLE_CHANGED', 'MFA_DISABLED', 'RETENTION_CHANGED') " +
           "AND ae.occurredAt >= :since " +
           "ORDER BY ae.occurredAt DESC")
    List<AuditEvent> findRecentPolicyChanges(@Param("since") Instant since);

    /**
     * Find recent assignment events for a doctor
     */
    @Query("SELECT ae FROM AuditEvent ae " +
           "WHERE ae.eventType = 'ASSIGNMENT_CREATED' " +
           "AND ae.actorUserId = :doctorId " +
           "AND ae.occurredAt >= :since " +
           "ORDER BY ae.occurredAt DESC")
    List<AuditEvent> findRecentDoctorAssignments(@Param("doctorId") Long doctorId, @Param("since") Instant since);

    /**
     * Find by actor user ID with pagination
     */
    Page<AuditEvent> findByActorUserId(Long actorUserId, Pageable pageable);

    /**
     * Find by patient ID with pagination
     */
    Page<AuditEvent> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Find by event type with pagination
     */
    Page<AuditEvent> findByEventType(String eventType, Pageable pageable);

    /**
     * Find by trace ID (for Zipkin correlation)
     */
    List<AuditEvent> findByTraceId(String traceId);
}
