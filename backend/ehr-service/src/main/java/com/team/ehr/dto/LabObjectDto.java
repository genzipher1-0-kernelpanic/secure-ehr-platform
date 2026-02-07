package com.team.ehr.dto;

import com.team.ehr.entity.LabReportType;
import java.time.Instant;
import java.time.LocalDate;

public class LabObjectDto {

    private String objectId;
    private LabReportType reportType;
    private String title;
    private LocalDate studyDate;
    private String mimeType;
    private Long sizeBytes;
    private Instant createdAt;
    private Integer relatedVersion;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getRelatedVersion() {
        return relatedVersion;
    }

    public void setRelatedVersion(Integer relatedVersion) {
        this.relatedVersion = relatedVersion;
    }
}
