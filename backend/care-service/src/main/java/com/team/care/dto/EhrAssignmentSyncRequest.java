package com.team.care.dto;

public class EhrAssignmentSyncRequest {

    public enum Action {
        ASSIGN,
        END
    }

    private Long patientId;
    private Long doctorUserId;
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
