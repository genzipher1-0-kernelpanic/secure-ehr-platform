package com.team.care.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class InternalProfileCreateRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Role role;

    @Valid
    private PatientProfileDto patientProfile;

    @Valid
    private DoctorProfileDto doctorProfile;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public PatientProfileDto getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(PatientProfileDto patientProfile) {
        this.patientProfile = patientProfile;
    }

    public DoctorProfileDto getDoctorProfile() {
        return doctorProfile;
    }

    public void setDoctorProfile(DoctorProfileDto doctorProfile) {
        this.doctorProfile = doctorProfile;
    }
}
