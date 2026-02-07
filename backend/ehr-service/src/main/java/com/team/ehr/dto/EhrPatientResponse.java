package com.team.ehr.dto;

public class EhrPatientResponse {

    private Long patientId;
    private EhrRecordDto clinical;
    private EhrRecordDto treatments;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public EhrRecordDto getClinical() {
        return clinical;
    }

    public void setClinical(EhrRecordDto clinical) {
        this.clinical = clinical;
    }

    public EhrRecordDto getTreatments() {
        return treatments;
    }

    public void setTreatments(EhrRecordDto treatments) {
        this.treatments = treatments;
    }
}
