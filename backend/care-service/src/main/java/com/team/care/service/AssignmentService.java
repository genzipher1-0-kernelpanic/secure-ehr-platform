package com.team.care.service;

import com.team.care.dto.AssignmentCreateRequest;
import com.team.care.entity.DoctorAssignment;
import com.team.care.repository.DoctorAssignmentRepository;
import com.team.care.repository.DoctorRepository;
import com.team.care.repository.PatientRepository;
import com.team.care.service.exception.ConflictException;
import com.team.care.service.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    private final DoctorAssignmentRepository assignmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final EventPublisher eventPublisher;
    private final EhrAssignmentSyncClient ehrAssignmentSyncClient;

    public AssignmentService(DoctorAssignmentRepository assignmentRepository,
                             PatientRepository patientRepository,
                             DoctorRepository doctorRepository,
                             EventPublisher eventPublisher,
                             EhrAssignmentSyncClient ehrAssignmentSyncClient) {
        this.assignmentRepository = assignmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.eventPublisher = eventPublisher;
        this.ehrAssignmentSyncClient = ehrAssignmentSyncClient;
    }

    @Transactional
    public Long createAssignment(AssignmentCreateRequest request) {
        if (!patientRepository.existsById(request.getPatientId())) {
            throw new NotFoundException("Patient not found");
        }
        if (!doctorRepository.existsByUserId(request.getDoctorUserId())) {
            throw new NotFoundException("Doctor not found");
        }
        boolean exists = assignmentRepository.existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(
                request.getPatientId(),
                request.getDoctorUserId()
        );
        if (exists) {
            throw new ConflictException("Active assignment already exists");
        }

        DoctorAssignment assignment = new DoctorAssignment();
        assignment.setPatientId(request.getPatientId());
        assignment.setDoctorUserId(request.getDoctorUserId());
        assignment.setReason(request.getReason());
        Long assignmentId = assignmentRepository.save(assignment).getId();

        ehrAssignmentSyncClient.assign(request.getPatientId(), request.getDoctorUserId());
        eventPublisher.publishAudit("ASSIGNMENT_CREATED", request.getPatientId(), request.getDoctorUserId(), "DOCTOR");
        return assignmentId;
    }

    @Transactional
    public void endAssignment(Long assignmentId) {
        DoctorAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
        if (assignment.getEndedAt() == null) {
            assignment.setEndedAt(Instant.now());
            assignmentRepository.save(assignment);
            ehrAssignmentSyncClient.end(assignment.getPatientId(), assignment.getDoctorUserId());
            eventPublisher.publishAudit("ASSIGNMENT_ENDED",
                    assignment.getPatientId(),
                    assignment.getDoctorUserId(),
                    "DOCTOR");
        }
    }
}
