package com.team.ehr.service;

import com.team.ehr.entity.EhrAuditLog;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.repository.EhrAuditLogRepository;
import com.team.ehr.security.SecurityUtil;
import com.team.ehr.security.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final EhrAuditLogRepository auditLogRepository;

    public AuditService(EhrAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, Long patientId, EhrCategory category, Long ehrId, String objectId, Integer newVersion) {
        EhrAuditLog log = new EhrAuditLog();
        log.setAction(action);
        log.setPatientId(patientId);
        log.setCategory(category);
        log.setEhrId(ehrId);
        log.setObjectId(objectId);
        log.setNewVersion(newVersion);
        log.setCreatedByUserId(SecurityUtil.getUserId());
        UserRole role = SecurityUtil.getRole();
        log.setCreatedByRole(role.name());
        auditLogRepository.save(log);
    }
}
