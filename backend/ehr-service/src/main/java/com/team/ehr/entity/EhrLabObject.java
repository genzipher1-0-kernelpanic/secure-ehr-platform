package com.team.ehr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "ehr_lab_object",
        indexes = {
                @Index(name = "idx_ehr_lab_patient", columnList = "patient_id"),
                @Index(name = "idx_ehr_lab_object", columnList = "object_id")
        }
)
public class EhrLabObject {

    @Id
    @Column(name = "object_id", length = 64)
    private String objectId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "object_path", nullable = false, length = 500)
    private String objectPath;

    @Column(name = "encrypted_data_key", nullable = false)
    private byte[] encryptedDataKey;

    @Column(name = "file_hash", nullable = false, length = 128)
    private String fileHash;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_by_role", nullable = false, length = 20)
    private String createdByRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private LabReportType reportType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "study_date")
    private LocalDate studyDate;

    @Column(name = "related_ehr_id")
    private Long relatedEhrId;

    @Column(name = "related_version")
    private Integer relatedVersion;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public byte[] getEncryptedDataKey() {
        return encryptedDataKey;
    }

    public void setEncryptedDataKey(byte[] encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
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

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
    }

    public Instant getCreatedAt() {
        return createdAt;
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

    public Long getRelatedEhrId() {
        return relatedEhrId;
    }

    public void setRelatedEhrId(Long relatedEhrId) {
        this.relatedEhrId = relatedEhrId;
    }

    public Integer getRelatedVersion() {
        return relatedVersion;
    }

    public void setRelatedVersion(Integer relatedVersion) {
        this.relatedVersion = relatedVersion;
    }
}
