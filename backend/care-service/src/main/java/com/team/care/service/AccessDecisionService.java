package com.team.care.service;

import com.team.care.dto.AccessDecisionResponse;
import com.team.care.entity.ConsentScope;
import com.team.care.repository.ConsentRepository;
import com.team.care.repository.DoctorAssignmentRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AccessDecisionService {

    private final DoctorAssignmentRepository assignmentRepository;
    private final ConsentRepository consentRepository;

    public AccessDecisionService(DoctorAssignmentRepository assignmentRepository,
                                 ConsentRepository consentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.consentRepository = consentRepository;
    }

    public AccessDecisionResponse checkAccess(Long doctorUserId, Long patientId, ConsentScope scope) {
        boolean assigned = assignmentRepository
                .existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(patientId, doctorUserId);
        if (assigned) {
            return new AccessDecisionResponse(true, "ASSIGNED");
        }

        boolean consented = consentRepository.existsActiveConsent(
                patientId,
                doctorUserId,
                scope,
                Instant.now()
        );
        if (consented) {
            return new AccessDecisionResponse(true, "CONSENT");
        }

        return new AccessDecisionResponse(false, "NO_ASSIGNMENT_OR_CONSENT");
    }
}
