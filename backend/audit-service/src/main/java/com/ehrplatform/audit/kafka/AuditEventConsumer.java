package com.ehrplatform.audit.kafka;

import com.ehrplatform.audit.dto.AuditEventMessage;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.service.AlertDetectionService;
import com.ehrplatform.audit.service.AuditEventStoreService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka consumer for multiple topics from care-service.
 * Consumes from 'audit-events', 'user-registered', 'patient-assign' topics.
 * Normalizes different payload formats to AuditEventMessage before storing.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditEventStoreService auditEventStoreService;
    private final AlertDetectionService alertDetectionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"audit-events", "user-registered", "patient-assign"},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String topic = record.topic();
        String payload = record.value();
        
        log.debug("Received message: topic={}, offset={}, key={}, payload={}",
                topic, record.offset(), record.key(), payload);

        try {
            // Parse JSON payload
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Normalize payload based on topic
            AuditEventMessage message = normalizePayload(topic, jsonNode);
            
            if (message == null || message.getEventType() == null || message.getEventType().isEmpty()) {
                log.warn("Skipping message with no eventType: topic={}, offset={}", topic, record.offset());
                ack.acknowledge();
                return;
            }

            // Store the event with hash chaining
            AuditEvent storedEvent = auditEventStoreService.storeEvent(message);
            
            // Check for immediate alerts
            alertDetectionService.checkImmediateAlerts(storedEvent);

            // Acknowledge successful processing
            ack.acknowledge();
            
            log.info("Successfully processed: topic={}, id={}, eventType={}, patientId={}",
                    topic, storedEvent.getId(), storedEvent.getEventType(), storedEvent.getPatientId());

        } catch (Exception e) {
            log.error("Error processing message: topic={}, offset={}, error={}", 
                    topic, record.offset(), e.getMessage(), e);
            // Don't acknowledge - will be retried, then sent to DLT
            throw new RuntimeException(e);
        }
    }

    /**
     * Normalize different topic payloads to AuditEventMessage format.
     */
    private AuditEventMessage normalizePayload(String topic, JsonNode node) {
        AuditEventMessage msg = AuditEventMessage.builder()
                .requestId(getTextOrDefault(node, "requestId", UUID.randomUUID().toString()))
                .occurredAt(parseInstant(node, "occurredAt"))
                .sourceService(getTextOrDefault(node, "sourceService", "care-service"))
                .severity(getTextOrDefault(node, "severity", "INFO"))
                .outcome(getTextOrDefault(node, "outcome", "SUCCESS"))
                .build();

        Map<String, Object> details = new HashMap<>();

        switch (topic) {
            case "audit-events":
                // Direct mapping for audit-events topic
                msg.setEventType(getTextOrNull(node, "eventType"));
                msg.setPatientId(getLongOrNull(node, "patientId"));
                msg.setDoctorUserId(getLongOrNull(node, "doctorUserId"));
                msg.setRole(getTextOrNull(node, "role"));
                msg.setActorUserId(getLongOrNull(node, "actorUserId"));
                msg.setActorEmail(getTextOrNull(node, "actorEmail"));
                msg.setIp(getTextOrNull(node, "ip"));
                break;

            case "user-registered":
                // Normalize user-registered payload
                msg.setEventType("USER_REGISTERED");
                msg.setActorEmail(getTextOrNull(node, "userEmail"));
                msg.setRole(getTextOrNull(node, "role"));
                
                // Store original payload as details
                details.put("userName", getTextOrNull(node, "userName"));
                details.put("userEmail", getTextOrNull(node, "userEmail"));
                details.put("role", getTextOrNull(node, "role"));
                msg.setDetails(details);
                break;

            case "patient-assign":
                // Normalize patient-assign payload
                msg.setEventType("PATIENT_ASSIGNED");
                msg.setPatientId(getLongOrNull(node, "patientId"));
                msg.setDoctorUserId(getLongOrNull(node, "doctorId"));
                
                // Store original payload as details
                details.put("patientId", getLongOrNull(node, "patientId"));
                details.put("patientName", getTextOrNull(node, "patientName"));
                details.put("doctorId", getLongOrNull(node, "doctorId"));
                details.put("doctorEmail", getTextOrNull(node, "doctorEmail"));
                msg.setDetails(details);
                
                // Set actor as doctor
                msg.setActorEmail(getTextOrNull(node, "doctorEmail"));
                break;

            default:
                log.warn("Unknown topic: {}", topic);
                return null;
        }

        return msg;
    }

    private String getTextOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        String value = getTextOrNull(node, field);
        return value != null ? value : defaultValue;
    }

    private Long getLongOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asLong() : null;
    }

    private Instant parseInstant(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            try {
                return Instant.parse(node.get(field).asText());
            } catch (Exception e) {
                log.warn("Failed to parse instant from field {}: {}", field, node.get(field).asText());
            }
        }
        return Instant.now();
    }
}
