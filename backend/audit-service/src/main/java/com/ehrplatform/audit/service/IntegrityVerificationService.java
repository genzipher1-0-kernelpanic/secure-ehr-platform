package com.ehrplatform.audit.service;

import com.ehrplatform.audit.dto.IntegrityVerifyResponse;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.entity.IntegrityCheckRun;
import com.ehrplatform.audit.repository.AuditEventRepository;
import com.ehrplatform.audit.repository.IntegrityCheckRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for verifying the integrity of the audit hash chain.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntegrityVerificationService {

    private final AuditEventRepository auditEventRepository;
    private final IntegrityCheckRunRepository integrityCheckRunRepository;
    private final HashChainService hashChainService;

    /**
     * Verify the hash chain integrity for a range of events.
     *
     * @param fromId Starting event ID (inclusive)
     * @param toId Ending event ID (inclusive)
     * @return Verification result
     */
    @Transactional
    public IntegrityVerifyResponse verifyRange(Long fromId, Long toId) {
        Instant startedAt = Instant.now();

        // Create check run record
        IntegrityCheckRun checkRun = IntegrityCheckRun.builder()
                .startedAt(startedAt)
                .fromEventId(fromId)
                .toEventId(toId)
                .status("RUNNING")
                .build();
        checkRun = integrityCheckRunRepository.save(checkRun);

        try {
            // Get events in range
            List<AuditEvent> events = auditEventRepository.findByIdBetweenOrderByIdAsc(fromId, toId);
            
            if (events.isEmpty()) {
                return completeCheck(checkRun, "FAIL", null, null, null, 
                        "No events found in range");
            }

            // Get the previous event's hash (or genesis if first event)
            String expectedPrevHash;
            if (fromId == 1) {
                expectedPrevHash = hashChainService.getGenesisHash();
            } else {
                AuditEvent prevEvent = auditEventRepository.findEventBeforeId(fromId)
                        .orElse(null);
                if (prevEvent == null) {
                    expectedPrevHash = hashChainService.getGenesisHash();
                } else {
                    expectedPrevHash = prevEvent.getEventHash();
                }
            }

            // Verify each event
            long eventsChecked = 0;
            Long lastVerifiedId = null;

            for (AuditEvent event : events) {
                // Check prev_hash matches expected
                if (!event.getPrevHash().equals(expectedPrevHash)) {
                    log.error("Integrity check FAILED at event {}: prevHash mismatch. Expected={}, Found={}",
                            event.getId(), expectedPrevHash, event.getPrevHash());
                    return completeCheck(checkRun, "FAIL", lastVerifiedId, 
                            expectedPrevHash, event.getPrevHash(),
                            "prevHash mismatch at event " + event.getId());
                }

                // Verify event hash is correctly computed
                if (!hashChainService.verifyEventHash(expectedPrevHash, event)) {
                    String recomputedHash = hashChainService.computeEventHash(expectedPrevHash, event);
                    log.error("Integrity check FAILED at event {}: eventHash mismatch. Expected={}, Found={}",
                            event.getId(), recomputedHash, event.getEventHash());
                    return completeCheck(checkRun, "FAIL", lastVerifiedId,
                            recomputedHash, event.getEventHash(),
                            "eventHash mismatch at event " + event.getId());
                }

                // Move to next
                expectedPrevHash = event.getEventHash();
                lastVerifiedId = event.getId();
                eventsChecked++;
            }

            log.info("Integrity check PASSED for events {} to {}. {} events verified.",
                    fromId, toId, eventsChecked);

            IntegrityCheckRun completed = completeCheckRun(checkRun, "OK", lastVerifiedId, null, null, null);
            
            return IntegrityVerifyResponse.builder()
                    .checkRunId(completed.getId())
                    .status("OK")
                    .fromEventId(fromId)
                    .toEventId(toId)
                    .lastVerifiedEventId(lastVerifiedId)
                    .totalEventsChecked(eventsChecked)
                    .startedAt(startedAt)
                    .finishedAt(completed.getFinishedAt())
                    .build();

        } catch (Exception e) {
            log.error("Integrity check failed with exception", e);
            return completeCheck(checkRun, "FAIL", null, null, null,
                    "Exception: " + e.getMessage());
        }
    }

    private IntegrityVerifyResponse completeCheck(IntegrityCheckRun checkRun, String status,
            Long lastVerifiedId, String expectedHash, String foundHash, String failReason) {
        
        IntegrityCheckRun completed = completeCheckRun(checkRun, status, lastVerifiedId, 
                expectedHash, foundHash, failReason);

        return IntegrityVerifyResponse.builder()
                .checkRunId(completed.getId())
                .status(status)
                .fromEventId(completed.getFromEventId())
                .toEventId(completed.getToEventId())
                .lastVerifiedEventId(lastVerifiedId)
                .expectedHash(expectedHash)
                .foundHash(foundHash)
                .failReason(failReason)
                .startedAt(completed.getStartedAt())
                .finishedAt(completed.getFinishedAt())
                .build();
    }

    private IntegrityCheckRun completeCheckRun(IntegrityCheckRun checkRun, String status,
            Long lastVerifiedId, String expectedHash, String foundHash, String failReason) {
        
        checkRun.setFinishedAt(Instant.now());
        checkRun.setStatus(status);
        checkRun.setLastVerifiedEventId(lastVerifiedId);
        checkRun.setExpectedHash(expectedHash);
        checkRun.setFoundHash(foundHash);
        checkRun.setFailReason(failReason);
        
        return integrityCheckRunRepository.save(checkRun);
    }
}
