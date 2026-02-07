package com.team.ehr.repository;

import com.team.ehr.entity.EhrLabObject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EhrLabObjectRepository extends JpaRepository<EhrLabObject, String> {
    List<EhrLabObject> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    Optional<EhrLabObject> findByPatientIdAndObjectId(Long patientId, String objectId);
}
