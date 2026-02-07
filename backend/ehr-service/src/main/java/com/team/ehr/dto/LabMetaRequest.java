package com.team.ehr.dto;

import com.team.ehr.entity.EhrCategory;
import com.team.ehr.entity.LabReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LabMetaRequest {

    @NotNull
    private LabReportType reportType;

    @NotBlank
    private String title;

    private LocalDate studyDate;

    private EhrCategory relatedCategory;

    private Integer relatedVersion;

    public LabReportType getReportType() {
        return reportType;
    }

    public void setReportType(LabReportType reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(LocalDate studyDate) {
        this.studyDate = studyDate;
    }

    public EhrCategory getRelatedCategory() {
        return relatedCategory;
    }

    public void setRelatedCategory(EhrCategory relatedCategory) {
        this.relatedCategory = relatedCategory;
    }

    public Integer getRelatedVersion() {
        return relatedVersion;
    }

    public void setRelatedVersion(Integer relatedVersion) {
        this.relatedVersion = relatedVersion;
    }
}
