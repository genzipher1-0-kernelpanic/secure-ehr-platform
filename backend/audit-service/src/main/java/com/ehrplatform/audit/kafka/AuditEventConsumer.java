package com.ehrplatform.audit.kafka;

import com.ehrplatform.audit.dto.AuditEventMessage;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.service.AlertDetectionService;
import com.ehrplatform.audit.service.AuditEventStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for audit events.
 * Consumes from 'audit-events' topic with manual acknowledgment.
 * Handles both minimal events (from care-service) and full events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditEventStoreService auditEventStoreService;
    private final AlertDetectionService alertDetectionService;

    @KafkaListener(
            topics = "${audit.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, AuditEventMessage> record, Acknowledgment ack) {
        AuditEventMessage message = record.value();
        
        log.debug("Received audit event: eventType={}, offset={}, key={}",
                message.getEventType(), record.offset(), record.key());

        try {
            // Validate minimal required field
            if (message.getEventType() == null || message.getEventType().isEmpty()) {
                log.error("Invalid audit event: eventType is required. Offset: {}", record.offset());
                // Acknowledge to avoid retry loop for invalid messages
                ack.acknowledge();
                return;
            }

            // Store the event with hash chaining (handles normalization internally)
            AuditEvent storedEvent = auditEventStoreService.storeEvent(message);
            
            // Check for immediate alerts (policy changes, assignments, etc.)
            alertDetectionService.checkImmediateAlerts(storedEvent);

            // Acknowledge successful processing
            ack.acknowledge();
            
            log.info("Successfully processed audit event: id={}, eventType={}, patientId={}",
                    storedEvent.getId(), storedEvent.getEventType(), storedEvent.getPatientId());

        } catch (Exception e) {
            log.error("Error processing audit event: eventType={}, offset={}", 
                    message.getEventType(), record.offset(), e);
            // Don't acknowledge - will be retried
            // In production, consider dead letter queue after max retries
            throw e;
        }
    }
}
