package com.team.ehr.repository;

import com.team.ehr.entity.EhrAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EhrAssignmentRepository extends JpaRepository<EhrAssignment, Long> {
    boolean existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(Long patientId, Long doctorUserId);
    java.util.Optional<EhrAssignment> findByPatientIdAndDoctorUserIdAndEndedAtIsNull(Long patientId, Long doctorUserId);
}
