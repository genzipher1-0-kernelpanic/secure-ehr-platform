package com.ehrplatform.audit.service;

import com.ehrplatform.audit.entity.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service for computing SHA-256 hashes for tamper-evident audit chain.
 */
@Service
@Slf4j
public class HashChainService {

    private final String genesisHash;

    public HashChainService(@Value("${audit.hash.genesis}") String genesisHash) {
        this.genesisHash = genesisHash;
    }

    /**
     * Get the genesis hash for the first event in the chain
     */
    public String getGenesisHash() {
        return sha256(genesisHash);
    }

    /**
     * Compute event hash for a new audit event
     *
     * @param prevHash The hash of the previous event (or genesis hash if first)
     * @param event The audit event to hash
     * @return SHA-256 hash string (64 hex characters)
     */
    public String computeEventHash(String prevHash, AuditEvent event) {
        String canonicalString = buildCanonicalString(event);
        String hashInput = prevHash + "|" + canonicalString;
        return sha256(hashInput);
    }

    /**
     * Verify that an event's hash is correct given the previous hash
     */
    public boolean verifyEventHash(String prevHash, AuditEvent event) {
        String expectedHash = computeEventHash(prevHash, event);
        return expectedHash.equals(event.getEventHash());
    }

    /**
     * Build a canonical string representation of an event.
     * Uses stable field ordering for reproducibility.
     */
    private String buildCanonicalString(AuditEvent event) {
        StringBuilder sb = new StringBuilder();
        
        // Fixed order of fields for canonical representation
        sb.append("occurredAt=").append(event.getOccurredAt()).append(";");
        sb.append("sourceService=").append(nullSafe(event.getSourceService())).append(";");
        sb.append("sourceInstance=").append(nullSafe(event.getSourceInstance())).append(";");
        sb.append("eventType=").append(nullSafe(event.getEventType())).append(";");
        sb.append("outcome=").append(nullSafe(event.getOutcome())).append(";");
        sb.append("severity=").append(nullSafe(event.getSeverity())).append(";");
        sb.append("actorUserId=").append(event.getActorUserId()).append(";");
        sb.append("actorRole=").append(nullSafe(event.getActorRole())).append(";");
        sb.append("actorEmail=").append(nullSafe(event.getActorEmail())).append(";");
        sb.append("ip=").append(nullSafe(event.getIp())).append(";");
        sb.append("userAgent=").append(nullSafe(event.getUserAgent())).append(";");
        sb.append("deviceId=").append(nullSafe(event.getDeviceId())).append(";");
        sb.append("sessionId=").append(nullSafe(event.getSessionId())).append(";");
        sb.append("patientId=").append(event.getPatientId()).append(";");
        sb.append("recordId=").append(event.getRecordId()).append(";");
        sb.append("targetUserId=").append(event.getTargetUserId()).append(";");
        sb.append("requestId=").append(nullSafe(event.getRequestId())).append(";");
        sb.append("traceId=").append(nullSafe(event.getTraceId())).append(";");
        sb.append("spanId=").append(nullSafe(event.getSpanId())).append(";");
        sb.append("details=").append(canonicalizeDetails(event.getDetailsJson()));

        return sb.toString();
    }

    /**
     * Canonicalize JSON details map (sorted keys)
     */
    private String canonicalizeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return "{}";
        }
        
        // TreeMap ensures sorted keys
        TreeMap<String, Object> sorted = new TreeMap<>(details);
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"")
              .append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Compute SHA-256 hash of a string
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
