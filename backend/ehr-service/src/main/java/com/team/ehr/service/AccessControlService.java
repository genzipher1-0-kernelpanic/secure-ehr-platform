package com.team.ehr.service;

import com.team.ehr.entity.EhrCategory;
import com.team.ehr.repository.EhrAssignmentRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    public AccessControlService(EhrAssignmentRepository assignmentRepository) {
        // kept for wiring compatibility
    }

    public void assertCanRead(Long patientId, EhrCategory category) {
        // Role-based authorization disabled.
    }

    public void assertCanUpdate(Long patientId, EhrCategory category) {
        // Role-based authorization disabled.
    }

    public void assertCanAccessLabs(Long patientId) {
        // Role-based authorization disabled.
    }

    public void assertCanExport(Long patientId) {
        // Role-based authorization disabled.
    }

    public boolean canRead(Long patientId, EhrCategory category) {
        return true;
    }

    public boolean canAccessLabs(Long patientId) {
        return true;
    }

    public boolean canExport(Long patientId) {
        return true;
    }

    private boolean isAssigned(Long patientId, Long doctorUserId) {
        return true;
    }

    public void assertDoctorAssigned(Long patientId) {
        // Role-based authorization disabled.
    }

}
