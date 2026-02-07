-- ============================================================
-- EHR Database Initialization
-- Electronic Health Records storage and versioning
-- ============================================================

CREATE DATABASE IF NOT EXISTS ehr_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE ehr_db;

-- ============================================================
-- Current EHR Records (latest version only)
-- ============================================================
CREATE TABLE IF NOT EXISTS ehr_record_current (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    category            VARCHAR(20) NOT NULL,           -- DEMOGRAPHICS, VITALS, ALLERGIES, MEDICATIONS, DIAGNOSES, NOTES
    current_version     INT NOT NULL,
    ciphertext          LONGBLOB NOT NULL,              -- AES-encrypted JSON
    key_id              VARCHAR(50) NOT NULL,           -- encryption key identifier
    content_hash        VARCHAR(128) NOT NULL,          -- SHA-256 hash of plaintext
    optimistic_version  BIGINT NOT NULL,                -- for optimistic locking
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_ehr_current_patient_category UNIQUE (patient_id, category),
    INDEX idx_ehr_current_patient (patient_id)
) ENGINE=InnoDB;

-- ============================================================
-- EHR Record Version History
-- ============================================================
CREATE TABLE IF NOT EXISTS ehr_record_version (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ehr_id              BIGINT NOT NULL,                -- FK to ehr_record_current.id
    patient_id          BIGINT NOT NULL,
    category            VARCHAR(20) NOT NULL,
    version             INT NOT NULL,
    ciphertext          LONGBLOB NOT NULL,
    key_id              VARCHAR(50) NOT NULL,
    content_hash        VARCHAR(128) NOT NULL,
    created_by_user_id  BIGINT NOT NULL,
    created_by_role     VARCHAR(20) NOT NULL,           -- DOCTOR, NURSE, ADMIN
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ehr_version_ehr_id (ehr_id),
    INDEX idx_ehr_version_patient (patient_id),
    INDEX idx_ehr_version_category (category),
    INDEX idx_ehr_version_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- Doctor-Patient Assignments (for EHR access control)
-- ============================================================
CREATE TABLE IF NOT EXISTS ehr_assignment (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    doctor_user_id      BIGINT NOT NULL,
    assigned_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at            TIMESTAMP NULL,

    INDEX idx_ehr_assignment_patient (patient_id),
    INDEX idx_ehr_assignment_doctor (doctor_user_id),
    INDEX idx_ehr_assignment_active (patient_id, doctor_user_id, ended_at)
) ENGINE=InnoDB;

-- ============================================================
-- Lab Objects (files: lab reports, imaging, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS ehr_lab_object (
    object_id           VARCHAR(64) PRIMARY KEY,        -- UUID
    patient_id          BIGINT NOT NULL,
    object_path         VARCHAR(500) NOT NULL,          -- storage path
    encrypted_data_key  LONGBLOB NOT NULL,              -- envelope-encrypted DEK
    file_hash           VARCHAR(128) NOT NULL,          -- SHA-256 of original file
    mime_type           VARCHAR(100) NOT NULL,
    size_bytes          BIGINT NOT NULL,
    created_by_user_id  BIGINT NOT NULL,
    created_by_role     VARCHAR(20) NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Lab-specific metadata
    report_type         VARCHAR(30) NOT NULL,           -- BLOOD, URINE, XRAY, MRI, CT, PATHOLOGY
    title               VARCHAR(200) NOT NULL,
    study_date          DATE NULL,
    related_ehr_id      BIGINT NULL,                    -- optional link to ehr_record_current
    related_version     INT NULL,

    INDEX idx_ehr_lab_patient (patient_id),
    INDEX idx_ehr_lab_report_type (report_type),
    INDEX idx_ehr_lab_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- EHR Audit Log (local audit for ehr-service)
-- ============================================================
CREATE TABLE IF NOT EXISTS ehr_audit_log (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    action              VARCHAR(40) NOT NULL,           -- CREATE, READ, UPDATE, DELETE, EXPORT
    patient_id          BIGINT NULL,
    category            VARCHAR(20) NULL,
    ehr_id              BIGINT NULL,
    object_id           VARCHAR(64) NULL,
    new_version         INT NULL,
    created_by_user_id  BIGINT NOT NULL,
    created_by_role     VARCHAR(20) NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ehr_audit_patient (patient_id),
    INDEX idx_ehr_audit_user (created_by_user_id),
    INDEX idx_ehr_audit_action (action),
    INDEX idx_ehr_audit_created (created_at)
) ENGINE=InnoDB;
