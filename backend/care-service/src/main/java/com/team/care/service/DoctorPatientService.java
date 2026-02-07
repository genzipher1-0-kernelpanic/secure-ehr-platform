package com.team.care.service;

import com.team.care.dto.PatientProfileDto;
import com.team.care.entity.DoctorAssignment;
import com.team.care.entity.Patient;
import com.team.care.repository.DoctorAssignmentRepository;
import com.team.care.repository.PatientRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DoctorPatientService {

    private final DoctorAssignmentRepository assignmentRepository;
    private final PatientRepository patientRepository;

    public DoctorPatientService(DoctorAssignmentRepository assignmentRepository,
                                PatientRepository patientRepository) {
        this.assignmentRepository = assignmentRepository;
        this.patientRepository = patientRepository;
    }

    public List<PatientProfileDto> listAssignedPatients(Long doctorUserId) {
        List<DoctorAssignment> assignments = assignmentRepository
                .findByDoctorUserIdAndEndedAtIsNull(doctorUserId);
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Long> patientIds = assignments.stream()
                .map(DoctorAssignment::getPatientId)
                .toList();
        Map<Long, Patient> patientMap = patientRepository.findAllById(patientIds).stream()
                .collect(Collectors.toMap(Patient::getId, Function.identity()));

        List<PatientProfileDto> result = new ArrayList<>();
        for (DoctorAssignment assignment : assignments) {
            Patient patient = patientMap.get(assignment.getPatientId());
            if (patient != null) {
                result.add(toDto(patient));
            }
        }
        return result;
    }

    private PatientProfileDto toDto(Patient patient) {
        PatientProfileDto dto = new PatientProfileDto();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setFullName(patient.getFullName());
        dto.setDob(patient.getDob());
        dto.setSex(patient.getSex());
        dto.setPhone(patient.getPhone());
        dto.setEmail(patient.getEmail());
        dto.setAddress(patient.getAddress());
        dto.setEmergencyContact(patient.getEmergencyContact());
        return dto;
    }
}
