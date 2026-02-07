package com.team.care.service;

import com.team.care.dto.AuditEvent;
import com.team.care.dto.PatientAssignEvent;
import com.team.care.dto.UserRegisteredEvent;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String patientAssignTopic;
    private final String userRegisteredTopic;
    private final String auditEventsTopic;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${kafka.topics.patientAssign}") String patientAssignTopic,
                          @Value("${kafka.topics.userRegistered}") String userRegisteredTopic,
                          @Value("${kafka.topics.auditEvents}") String auditEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.patientAssignTopic = patientAssignTopic;
        this.userRegisteredTopic = userRegisteredTopic;
        this.auditEventsTopic = auditEventsTopic;
    }

    public void publishPatientAssign(PatientAssignEvent event) {
        kafkaTemplate.send(patientAssignTopic, String.valueOf(event.getPatientId()), event);
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(userRegisteredTopic, event.getUserEmail(), event);
    }

    public void publishAudit(String eventType, Long patientId, Long doctorUserId, String role) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setPatientId(patientId);
        event.setDoctorUserId(doctorUserId);
        event.setRole(role);
        event.setOccurredAt(Instant.now());
        kafkaTemplate.send(auditEventsTopic, eventType, event);
    }
}
