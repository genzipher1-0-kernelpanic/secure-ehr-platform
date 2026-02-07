-- ============================================================
-- Care Database Initialization
-- Patient-Doctor assignments and consents
-- ============================================================

CREATE DATABASE IF NOT EXISTS care_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE care_db;

-- ============================================================
-- Patient Profiles
-- ============================================================
CREATE TABLE IF NOT EXISTS patients (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,         -- FK to identity-service user
    full_name           VARCHAR(200) NOT NULL,
    dob                 DATE NULL,
    sex                 ENUM('MALE','FEMALE') NULL,
    phone               VARCHAR(30) NULL,
    email               VARCHAR(200) NULL,
    address             VARCHAR(300) NULL,
    emergency_contact   VARCHAR(200) NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_patients_email (email),
    INDEX idx_patients_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- Doctor Profiles
-- ============================================================
CREATE TABLE IF NOT EXISTS doctors (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL UNIQUE,         -- FK to identity-service user
    full_name           VARCHAR(200) NOT NULL,
    specialization      VARCHAR(150) NULL,
    license_number      VARCHAR(100) NULL,
    phone               VARCHAR(30) NULL,
    email               VARCHAR(200) NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_doctors_email (email),
    INDEX idx_doctors_specialization (specialization)
) ENGINE=InnoDB;

-- ============================================================
-- Doctor-Patient Assignments
-- ============================================================
CREATE TABLE IF NOT EXISTS doctor_assignments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    doctor_user_id      BIGINT NOT NULL,
    assigned_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at            TIMESTAMP NULL,
    reason              VARCHAR(200) NULL,

    INDEX idx_assignments_patient_id (patient_id),
    INDEX idx_assignments_doctor_user_id (doctor_user_id),
    INDEX idx_assignments_active (patient_id, doctor_user_id, ended_at),

    CONSTRAINT fk_assignments_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Patient Consents (for data access permissions)
-- ============================================================
CREATE TABLE IF NOT EXISTS consents (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT NOT NULL,
    grantee_user_id     BIGINT NOT NULL,                -- user who receives consent
    scope               ENUM('EHR_READ','EHR_WRITE','APPOINTMENT_READ','APPOINTMENT_WRITE','ALL') NOT NULL,
    valid_from          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_to            TIMESTAMP NULL,
    revoked_at          TIMESTAMP NULL,

    INDEX idx_consents_patient_id (patient_id),
    INDEX idx_consents_grantee_user_id (grantee_user_id),
    INDEX idx_consents_valid_window (valid_from, valid_to),

    CONSTRAINT fk_consents_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
