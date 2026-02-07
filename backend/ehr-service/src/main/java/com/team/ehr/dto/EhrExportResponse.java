package com.team.ehr.dto;

import java.util.List;

public class EhrExportResponse {

    private Long patientId;
    private EhrRecordDto clinical;
    private EhrRecordDto treatments;
    private List<LabObjectDto> labs;

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

    public List<LabObjectDto> getLabs() {
        return labs;
    }

    public void setLabs(List<LabObjectDto> labs) {
        this.labs = labs;
    }
}
