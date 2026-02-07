package com.ehrplatform.audit.service;

import com.ehrplatform.audit.entity.Alert;
import com.ehrplatform.audit.entity.AlertDedupKey;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.kafka.AlertPublisher;
import com.ehrplatform.audit.repository.AlertDedupKeyRepository;
import com.ehrplatform.audit.repository.AlertRepository;
import com.ehrplatform.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for detecting security alerts based on audit events.
 * Implements 5 alert types:
 * 1. ADMIN_POLICY_CHANGE - Immediate on policy change events
 * 2. REPEATED_FAILED_LOGIN - Failed logins threshold
 * 3. DENIED_ACCESS_BURST - Denied access threshold
 * 4. EXPORT_SPIKE / BULK_ACCESS - Data export/access thresholds
 * 5. AUDIT_CHAIN_FAIL - Integrity verification failures
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertDetectionService {

    private final AlertRepository alertRepository;
    private final AlertDedupKeyRepository alertDedupKeyRepository;
    private final AuditEventRepository auditEventRepository;
    private final AlertPublisher alertPublisher;

    @Value("${audit.alerts.enabled:true}")
    private boolean alertsEnabled;

    @Value("${audit.alerts.thresholds.failed-login-count:5}")
    private int failedLoginThreshold;

    @Value("${audit.alerts.thresholds.failed-login-window-minutes:5}")
    private int failedLoginWindowMinutes;

    @Value("${audit.alerts.thresholds.denied-access-count:20}")
    private int deniedAccessThreshold;

    @Value("${audit.alerts.thresholds.denied-access-window-minutes:5}")
    private int deniedAccessWindowMinutes;

    @Value("${audit.alerts.thresholds.export-spike-count:10}")
    private int exportSpikeThreshold;

    @Value("${audit.alerts.thresholds.export-spike-window-minutes:10}")
    private int exportSpikeWindowMinutes;

    @Value("${audit.alerts.thresholds.bulk-access-patient-count:50}")
    private int bulkAccessPatientThreshold;

    @Value("${audit.alerts.thresholds.bulk-access-window-minutes:10}")
    private int bulkAccessWindowMinutes;

    // Policy change event types that trigger immediate alerts
    private static final Set<String> POLICY_CHANGE_EVENTS = Set.of(
            "ROLE_CHANGED", "MFA_DISABLED", "RETENTION_CHANGED",
            "ADMIN_CREATED", "ADMIN_DELETED", "PERMISSION_CHANGED"
    );

    // Care-service events that should be logged for audit trail (informational alerts)
    private static final Set<String> CARE_EVENTS = Set.of(
            "ASSIGNMENT_CREATED", "ASSIGNMENT_REMOVED", "CONSENT_GRANTED",
            "CONSENT_REVOKED", "PATIENT_ACCESSED", "RECORD_VIEWED"
    );

    /**
     * Check for immediate alerts on event ingest (e.g., policy changes, suspicious activity)
     */
    @Transactional
    public void checkImmediateAlerts(AuditEvent event) {
        if (!alertsEnabled) return;

        // Check for policy change events (CRITICAL/HIGH severity)
        if (POLICY_CHANGE_EVENTS.contains(event.getEventType())) {
            createPolicyChangeAlert(event);
            return;
        }

        // Log care-service events for tracking (no alert, just audit)
        if (CARE_EVENTS.contains(event.getEventType())) {
            log.info("Care event recorded: eventType={}, patientId={}, actorUserId={}",
                    event.getEventType(), event.getPatientId(), event.getActorUserId());
        }
    }

    /**
     * Scheduled job to detect pattern-based alerts
     */
    @Scheduled(fixedRateString = "${audit.alerts.schedule.fixed-rate:60000}")
    @Transactional
    public void runScheduledAlertDetection() {
        if (!alertsEnabled) {
            return;
        }

        log.debug("Running scheduled alert detection...");

        try {
            detectRepeatedFailedLogins();
            detectDeniedAccessBursts();
            detectExportSpikes();
            detectBulkPatientAccess();
            cleanupExpiredDedupKeys();
        } catch (Exception e) {
            log.error("Error during scheduled alert detection", e);
        }
    }

    /**
     * Create alert for policy change events (ADMIN_POLICY_CHANGE)
     */
    private void createPolicyChangeAlert(AuditEvent event) {
        String dedupKey = buildDedupKey("ADMIN_POLICY_CHANGE", 
                event.getEventType(), 
                String.valueOf(event.getActorUserId()),
                event.getOccurredAt().truncatedTo(ChronoUnit.MINUTES).toString());

        if (isDuplicate(dedupKey)) {
            return;
        }

        String severity = determinePolicySeverity(event.getEventType());
        
        Alert alert = Alert.builder()
                .alertType("ADMIN_POLICY_CHANGE")
                .severity(severity)
                .title("Policy Change Detected: " + event.getEventType())
                .message(String.format("User %s performed %s action. Target user: %s, IP: %s",
                        event.getActorEmail() != null ? event.getActorEmail() : event.getActorUserId(),
                        event.getEventType(),
                        event.getTargetUserId(),
                        event.getIp()))
                .actorUserId(event.getActorUserId())
                .actorEmail(event.getActorEmail())
                .ip(event.getIp())
                .evidenceJson(Map.of(
                        "eventId", event.getId(),
                        "eventType", event.getEventType(),
                        "occurredAt", event.getOccurredAt().toString()
                ))
                .build();

        Alert saved = alertRepository.save(alert);
        saveDedupKey(dedupKey, saved.getId(), 60); // 60 minute expiry
        alertPublisher.publishAlert(saved);
        
        log.warn("ALERT: {} - {}", alert.getAlertType(), alert.getTitle());
    }

    /**
     * Detect repeated failed login attempts (REPEATED_FAILED_LOGIN)
     */
    private void detectRepeatedFailedLogins() {
        Instant since = Instant.now().minus(failedLoginWindowMinutes, ChronoUnit.MINUTES);
        
        List<Object[]> results = auditEventRepository.findRepeatedLoginFailures(
                since, failedLoginThreshold);

        for (Object[] row : results) {
            String email = (String) row[0];
            String ip = (String) row[1];
            Long count = (Long) row[2];

            String dedupKey = buildDedupKey("REPEATED_FAILED_LOGIN", 
                    email != null ? email : "unknown",
                    ip != null ? ip : "unknown",
                    Instant.now().truncatedTo(ChronoUnit.HOURS).toString());

            if (isDuplicate(dedupKey)) {
                continue;
            }

            Alert alert = Alert.builder()
                    .alertType("REPEATED_FAILED_LOGIN")
                    .severity("HIGH")
                    .title("Repeated Failed Login Attempts")
                    .message(String.format("%d failed login attempts from email %s, IP %s in last %d minutes",
                            count, email, ip, failedLoginWindowMinutes))
                    .actorEmail(email)
                    .ip(ip)
                    .evidenceJson(Map.of(
                            "failedCount", count,
                            "email", email != null ? email : "unknown",
                            "ip", ip != null ? ip : "unknown",
                            "windowMinutes", failedLoginWindowMinutes
                    ))
                    .build();

            Alert saved = alertRepository.save(alert);
            saveDedupKey(dedupKey, saved.getId(), 60);
            alertPublisher.publishAlert(saved);
            
            log.warn("ALERT: {} - {} attempts from {}/{}", 
                    alert.getAlertType(), count, email, ip);
        }
    }

    /**
     * Detect denied access bursts (DENIED_ACCESS_BURST)
     */
    private void detectDeniedAccessBursts() {
        Instant since = Instant.now().minus(deniedAccessWindowMinutes, ChronoUnit.MINUTES);
        
        List<Object[]> results = auditEventRepository.findDeniedAccessBursts(
                since, deniedAccessThreshold);

        for (Object[] row : results) {
            Long userId = (Long) row[0];
            String ip = (String) row[1];
            Long count = (Long) row[2];

            String dedupKey = buildDedupKey("DENIED_ACCESS_BURST",
                    String.valueOf(userId),
                    ip != null ? ip : "unknown",
                    Instant.now().truncatedTo(ChronoUnit.HOURS).toString());

            if (isDuplicate(dedupKey)) {
                continue;
            }

            Alert alert = Alert.builder()
                    .alertType("DENIED_ACCESS_BURST")
                    .severity("HIGH")
                    .title("Denied Access Burst Detected")
                    .message(String.format("%d access denials for user %d from IP %s in last %d minutes",
                            count, userId, ip, deniedAccessWindowMinutes))
                    .actorUserId(userId)
                    .ip(ip)
                    .evidenceJson(Map.of(
                            "deniedCount", count,
                            "userId", userId,
                            "ip", ip != null ? ip : "unknown",
                            "windowMinutes", deniedAccessWindowMinutes
                    ))
                    .build();

            Alert saved = alertRepository.save(alert);
            saveDedupKey(dedupKey, saved.getId(), 60);
            alertPublisher.publishAlert(saved);
            
            log.warn("ALERT: {} - {} denials for user {}", 
                    alert.getAlertType(), count, userId);
        }
    }

    /**
     * Detect export request spikes (EXPORT_SPIKE)
     */
    private void detectExportSpikes() {
        Instant since = Instant.now().minus(exportSpikeWindowMinutes, ChronoUnit.MINUTES);
        
        List<Object[]> results = auditEventRepository.findExportSpikes(
                since, exportSpikeThreshold);

        for (Object[] row : results) {
            Long userId = (Long) row[0];
            Long count = (Long) row[1];

            String dedupKey = buildDedupKey("EXPORT_SPIKE",
                    String.valueOf(userId),
                    Instant.now().truncatedTo(ChronoUnit.HOURS).toString());

            if (isDuplicate(dedupKey)) {
                continue;
            }

            Alert alert = Alert.builder()
                    .alertType("EXPORT_SPIKE")
                    .severity("HIGH")
                    .title("Unusual Export Activity")
                    .message(String.format("User %d requested %d exports in last %d minutes",
                            userId, count, exportSpikeWindowMinutes))
                    .actorUserId(userId)
                    .evidenceJson(Map.of(
                            "exportCount", count,
                            "userId", userId,
                            "windowMinutes", exportSpikeWindowMinutes
                    ))
                    .build();

            Alert saved = alertRepository.save(alert);
            saveDedupKey(dedupKey, saved.getId(), 60);
            alertPublisher.publishAlert(saved);
            
            log.warn("ALERT: {} - {} exports by user {}", 
                    alert.getAlertType(), count, userId);
        }
    }

    /**
     * Detect bulk patient access (BULK_ACCESS)
     */
    private void detectBulkPatientAccess() {
        Instant since = Instant.now().minus(bulkAccessWindowMinutes, ChronoUnit.MINUTES);
        
        List<Object[]> results = auditEventRepository.findBulkPatientAccess(
                since, bulkAccessPatientThreshold);

        for (Object[] row : results) {
            Long userId = (Long) row[0];
            Long patientCount = (Long) row[1];

            String dedupKey = buildDedupKey("BULK_ACCESS",
                    String.valueOf(userId),
                    Instant.now().truncatedTo(ChronoUnit.HOURS).toString());

            if (isDuplicate(dedupKey)) {
                continue;
            }

            Alert alert = Alert.builder()
                    .alertType("BULK_ACCESS")
                    .severity("CRITICAL")
                    .title("Bulk Patient Access Detected")
                    .message(String.format("User %d accessed %d unique patient records in last %d minutes",
                            userId, patientCount, bulkAccessWindowMinutes))
                    .actorUserId(userId)
                    .evidenceJson(Map.of(
                            "uniquePatientCount", patientCount,
                            "userId", userId,
                            "windowMinutes", bulkAccessWindowMinutes
                    ))
                    .build();

            Alert saved = alertRepository.save(alert);
            saveDedupKey(dedupKey, saved.getId(), 120); // 2 hour expiry for critical
            alertPublisher.publishAlert(saved);
            
            log.warn("ALERT: {} - {} patients accessed by user {}", 
                    alert.getAlertType(), patientCount, userId);
        }
    }

    /**
     * Create alert for audit chain integrity failure
     */
    @Transactional
    public void createIntegrityFailureAlert(Long checkRunId, String failReason) {
        String dedupKey = buildDedupKey("AUDIT_CHAIN_FAIL", 
                String.valueOf(checkRunId),
                Instant.now().truncatedTo(ChronoUnit.HOURS).toString());

        if (isDuplicate(dedupKey)) {
            return;
        }

        Alert alert = Alert.builder()
                .alertType("AUDIT_CHAIN_FAIL")
                .severity("CRITICAL")
                .title("Audit Chain Integrity Failure")
                .message("Audit log tamper detection triggered: " + failReason)
                .evidenceJson(Map.of(
                        "checkRunId", checkRunId,
                        "failReason", failReason
                ))
                .build();

        Alert saved = alertRepository.save(alert);
        saveDedupKey(dedupKey, saved.getId(), 1440); // 24 hour expiry for critical
        alertPublisher.publishAlert(saved);
        
        log.error("CRITICAL ALERT: {} - {}", alert.getAlertType(), alert.getMessage());
    }

    private String determinePolicySeverity(String eventType) {
        return switch (eventType) {
            case "MFA_DISABLED", "ADMIN_DELETED" -> "CRITICAL";
            case "ROLE_CHANGED", "PERMISSION_CHANGED" -> "HIGH";
            default -> "MED";
        };
    }

    private String buildDedupKey(String alertType, String... parts) {
        return alertType + ":" + String.join(":", parts);
    }

    private boolean isDuplicate(String dedupKey) {
        return alertDedupKeyRepository.existsByDedupKeyAndExpiresAtAfter(dedupKey, Instant.now());
    }

    private void saveDedupKey(String dedupKey, Long alertId, int expiryMinutes) {
        AlertDedupKey key = AlertDedupKey.builder()
                .dedupKey(dedupKey)
                .alertId(alertId)
                .expiresAt(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES))
                .build();
        alertDedupKeyRepository.save(key);
    }

    private void cleanupExpiredDedupKeys() {
        int deleted = alertDedupKeyRepository.deleteExpiredKeys(Instant.now());
        if (deleted > 0) {
            log.debug("Cleaned up {} expired dedup keys", deleted);
        }
    }
}
