package com.team.ehr.service;

import com.team.ehr.dto.AssignmentSyncRequest;
import com.team.ehr.entity.EhrAssignment;
import com.team.ehr.repository.EhrAssignmentRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AssignmentSyncService {

    private final EhrAssignmentRepository assignmentRepository;

    public AssignmentSyncService(EhrAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void sync(AssignmentSyncRequest request) {
        if (request.getAction() == AssignmentSyncRequest.Action.ASSIGN) {
            boolean exists = assignmentRepository
                    .existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(
                            request.getPatientId(), request.getDoctorUserId());
            if (!exists) {
                EhrAssignment assignment = new EhrAssignment();
                assignment.setPatientId(request.getPatientId());
                assignment.setDoctorUserId(request.getDoctorUserId());
                assignmentRepository.save(assignment);
            }
            return;
        }

        assignmentRepository.findByPatientIdAndDoctorUserIdAndEndedAtIsNull(
                        request.getPatientId(), request.getDoctorUserId())
                .ifPresent(assignment -> {
                    assignment.setEndedAt(Instant.now());
                    assignmentRepository.save(assignment);
                });
    }
}
