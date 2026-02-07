package com.ehrplatform.audit.kafka;

import com.ehrplatform.audit.dto.AuditEventMessage;
import com.ehrplatform.audit.entity.AuditEvent;
import com.ehrplatform.audit.service.AlertDetectionService;
import com.ehrplatform.audit.service.AuditEventStoreService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kafka consumer for audit events.
 * Consumes from 'audit.events' topic with manual acknowledgment.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditEventStoreService auditEventStoreService;
    private final AlertDetectionService alertDetectionService;
    private final Validator validator;

    @KafkaListener(
            topics = "${audit.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, AuditEventMessage> record, Acknowledgment ack) {
        AuditEventMessage message = record.value();
        
        log.debug("Received audit event: requestId={}, eventType={}, offset={}",
                message.getRequestId(), message.getEventType(), record.offset());

        try {
            // Validate required fields
            Set<ConstraintViolation<AuditEventMessage>> violations = validator.validate(message);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining(", "));
                log.error("Invalid audit event message: {}. Errors: {}", message.getRequestId(), errors);
                // Acknowledge to avoid retry loop for invalid messages
                ack.acknowledge();
                return;
            }

            // Store the event with hash chaining
            AuditEvent storedEvent = auditEventStoreService.storeEvent(message);
            
            // Check for immediate alerts (policy changes)
            alertDetectionService.checkImmediateAlerts(storedEvent);

            // Acknowledge successful processing
            ack.acknowledge();
            
            log.info("Successfully processed audit event: id={}, requestId={}, eventType={}",
                    storedEvent.getId(), message.getRequestId(), message.getEventType());

        } catch (Exception e) {
            log.error("Error processing audit event: requestId={}", message.getRequestId(), e);
            // Don't acknowledge - will be retried
            // In production, consider dead letter queue after max retries
            throw e;
        }
    }
}
