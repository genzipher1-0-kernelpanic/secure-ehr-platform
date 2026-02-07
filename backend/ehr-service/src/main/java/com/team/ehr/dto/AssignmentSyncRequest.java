package com.team.ehr.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentSyncRequest {

    public enum Action {
        ASSIGN,
        END
    }

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorUserId;

    @NotNull
    private Action action;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
