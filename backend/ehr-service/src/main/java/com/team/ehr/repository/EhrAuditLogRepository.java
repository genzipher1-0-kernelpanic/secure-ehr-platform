package com.team.ehr.repository;

import com.team.ehr.entity.EhrAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EhrAuditLogRepository extends JpaRepository<EhrAuditLog, Long> {
}
