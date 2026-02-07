package com.ehrplatform.audit.kafka;

import com.ehrplatform.audit.dto.AlertMessage;
import com.ehrplatform.audit.entity.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for publishing alerts to Kafka.
 * Notification-service consumes these alerts to send notifications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${audit.alerts.kafka.topic:alerts}")
    private String alertsTopic;

    /**
     * Publish an alert to Kafka for notification-service to consume.
     *
     * @param alert The alert entity to publish
     */
    public void publishAlert(Alert alert) {
        if (alert == null || alert.getId() == null) {
            log.warn("Cannot publish null alert or alert without ID");
            return;
        }

        AlertMessage message = AlertMessage.fromAlert(alert);
        String key = alert.getAlertType() + "-" + alert.getId();

        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(alertsTopic, key, message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish alert to Kafka: alertId={}, alertType={}, error={}",
                            alert.getId(), alert.getAlertType(), ex.getMessage());
                } else {
                    log.info("Published alert to Kafka: alertId={}, alertType={}, severity={}, topic={}, partition={}, offset={}",
                            alert.getId(),
                            alert.getAlertType(),
                            alert.getSeverity(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error publishing alert to Kafka: alertId={}, alertType={}",
                    alert.getId(), alert.getAlertType(), e);
        }
    }

    /**
     * Publish an alert synchronously (blocks until acknowledged or timeout).
     * Use this when you need to ensure the alert was published before proceeding.
     *
     * @param alert The alert entity to publish
     * @return true if published successfully, false otherwise
     */
    public boolean publishAlertSync(Alert alert) {
        if (alert == null || alert.getId() == null) {
            log.warn("Cannot publish null alert or alert without ID");
            return false;
        }

        AlertMessage message = AlertMessage.fromAlert(alert);
        String key = alert.getAlertType() + "-" + alert.getId();

        try {
            SendResult<String, Object> result = kafkaTemplate.send(alertsTopic, key, message).get();
            log.info("Published alert synchronously to Kafka: alertId={}, alertType={}, severity={}, offset={}",
                    alert.getId(),
                    alert.getAlertType(),
                    alert.getSeverity(),
                    result.getRecordMetadata().offset());
            return true;

        } catch (Exception e) {
            log.error("Error publishing alert synchronously to Kafka: alertId={}, alertType={}",
                    alert.getId(), alert.getAlertType(), e);
            return false;
        }
    }
}
