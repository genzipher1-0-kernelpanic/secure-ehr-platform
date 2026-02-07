package com.team.ehr.service;

import com.team.ehr.entity.EhrCategory;
import com.team.ehr.exception.ForbiddenException;
import com.team.ehr.repository.EhrAssignmentRepository;
import com.team.ehr.security.SecurityUtil;
import com.team.ehr.security.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private final EhrAssignmentRepository assignmentRepository;
    public AccessControlService(EhrAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public void assertCanRead(Long patientId, EhrCategory category) {
        if (!canRead(patientId, category)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public void assertCanUpdate(Long patientId, EhrCategory category) {
        if (!canRead(patientId, category)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public void assertCanAccessLabs(Long patientId) {
        if (!canAccessLabs(patientId)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public void assertCanExport(Long patientId) {
        if (!canExport(patientId)) {
            throw new ForbiddenException("Access denied");
        }
    }

    public boolean canRead(Long patientId, EhrCategory category) {
        UserRole role = SecurityUtil.getRole();
        Long userId = SecurityUtil.getUserId();
        if (role == UserRole.ADMIN) {
            return true;
        }
        if (role == UserRole.PATIENT && userId.equals(patientId)) {
            return true;
        }
        if (role == UserRole.DOCTOR) {
            return isAssigned(patientId, userId);
        }
        return false;
    }

    public boolean canAccessLabs(Long patientId) {
        UserRole role = SecurityUtil.getRole();
        Long userId = SecurityUtil.getUserId();
        if (role == UserRole.ADMIN) {
            return true;
        }
        if (role == UserRole.PATIENT && userId.equals(patientId)) {
            return true;
        }
        if (role == UserRole.DOCTOR) {
            return isAssigned(patientId, userId);
        }
        return false;
    }

    public boolean canExport(Long patientId) {
        UserRole role = SecurityUtil.getRole();
        Long userId = SecurityUtil.getUserId();
        if (role == UserRole.ADMIN) {
            return true;
        }
        if (role == UserRole.PATIENT && userId.equals(patientId)) {
            return true;
        }
        if (role == UserRole.DOCTOR) {
            return isAssigned(patientId, userId);
        }
        return false;
    }

    private boolean isAssigned(Long patientId, Long doctorUserId) {
        return assignmentRepository.existsByPatientIdAndDoctorUserIdAndEndedAtIsNull(patientId, doctorUserId);
    }

    public void assertDoctorAssigned(Long patientId) {
        if (SecurityUtil.getRole() != UserRole.DOCTOR) {
            throw new ForbiddenException("Doctor role required");
        }
        Long doctorUserId = SecurityUtil.getUserId();
        if (!isAssigned(patientId, doctorUserId)) {
            throw new ForbiddenException("Doctor not assigned to patient");
        }
    }

}
