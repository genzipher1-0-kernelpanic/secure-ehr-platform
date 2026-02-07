package com.team.care.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorUserId;

    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
