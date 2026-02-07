package com.team.ehr.repository;

import com.team.ehr.entity.EhrCategory;
import com.team.ehr.entity.EhrRecordCurrent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EhrRecordCurrentRepository extends JpaRepository<EhrRecordCurrent, Long> {
    Optional<EhrRecordCurrent> findByPatientIdAndCategory(Long patientId, EhrCategory category);
}
