package com.team.care.repository;

import com.team.care.entity.DoctorAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorAssignmentRepository extends JpaRepository<DoctorAssignment, Long> {
    boolean existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(Long patientId, Long doctorUserId);
    List<DoctorAssignment> findByDoctorUserIdAndEndedAtIsNull(Long doctorUserId);
}
