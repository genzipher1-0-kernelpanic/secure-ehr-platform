package com.team.ehr.repository;

import com.team.ehr.entity.EhrCategory;
import com.team.ehr.entity.EhrRecordVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EhrRecordVersionRepository extends JpaRepository<EhrRecordVersion, Long> {
    List<EhrRecordVersion> findByPatientIdAndCategoryOrderByVersionDesc(Long patientId, EhrCategory category);
    Optional<EhrRecordVersion> findByPatientIdAndCategoryAndVersion(Long patientId, EhrCategory category, Integer version);
    List<EhrRecordVersion> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
