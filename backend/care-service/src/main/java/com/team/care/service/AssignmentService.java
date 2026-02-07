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

    public AssignmentService(DoctorAssignmentRepository assignmentRepository,
                             PatientRepository patientRepository,
                             DoctorRepository doctorRepository,
                             EventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.eventPublisher = eventPublisher;
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

        com.team.care.dto.PatientAssignEvent event = new com.team.care.dto.PatientAssignEvent();
        event.setPatientId(request.getPatientId());
        event.setDoctorId(request.getDoctorUserId());
        patientRepository.findById(request.getPatientId())
                .ifPresent(patient -> event.setPatientName(patient.getFullName()));
        doctorRepository.findByUserId(request.getDoctorUserId())
                .ifPresent(doctor -> event.setDoctorEmail(doctor.getEmail()));
        eventPublisher.publishPatientAssign(event);
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
            eventPublisher.publishAudit("ASSIGNMENT_ENDED",
                    assignment.getPatientId(),
                    assignment.getDoctorUserId(),
                    "DOCTOR");
        }
    }
}
