CREATE DATABASE IF NOT EXISTS ehr_db;
USE ehr_db;

CREATE TABLE ehr_record_current (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    current_version INT NOT NULL,
    ciphertext LONGBLOB NOT NULL,
    key_id VARCHAR(50) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    optimistic_version BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_ehr_current_patient_category UNIQUE (patient_id, category)
) ENGINE=InnoDB;

CREATE TABLE ehr_record_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ehr_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    version INT NOT NULL,
    ciphertext LONGBLOB NOT NULL,
    key_id VARCHAR(50) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_by_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ehr_version_patient (patient_id),
    INDEX idx_ehr_version_category (category)
) ENGINE=InnoDB;

CREATE TABLE ehr_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_user_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    INDEX idx_ehr_assignment_patient (patient_id),
    INDEX idx_ehr_assignment_doctor (doctor_user_id)
) ENGINE=InnoDB;

CREATE TABLE ehr_lab_object (
    object_id VARCHAR(64) PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    object_path VARCHAR(500) NOT NULL,
    encrypted_data_key LONGBLOB NOT NULL,
    file_hash VARCHAR(128) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_by_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    report_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    study_date DATE NULL,
    related_ehr_id BIGINT NULL,
    related_version INT NULL,
    INDEX idx_ehr_lab_patient (patient_id),
    INDEX idx_ehr_lab_object (object_id)
) ENGINE=InnoDB;

CREATE TABLE ehr_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(40) NOT NULL,
    patient_id BIGINT NULL,
    category VARCHAR(20) NULL,
    ehr_id BIGINT NULL,
    object_id VARCHAR(64) NULL,
    new_version INT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_by_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
