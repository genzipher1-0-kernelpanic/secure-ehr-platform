package com.team.care.repository;

import com.team.care.entity.DoctorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorAssignmentRepository extends JpaRepository<DoctorAssignment, Long> {
    boolean existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(Long patientId, Long doctorUserId);
}
