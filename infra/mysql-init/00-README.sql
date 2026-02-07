-- ============================================================
-- Master Database Initialization Script
-- Runs all database init scripts in order
-- ============================================================

-- This file is for documentation/manual execution only.
-- Docker MySQL uses individual files in /docker-entrypoint-initdb.d/
-- which are executed in alphabetical order.

-- Files executed:
-- 1. 01-audit.sql  (audit_db)
-- 2. 02-care.sql   (care_db)
-- 3. 03-ehr.sql    (ehr_db)

-- To run manually:
-- mysql -u root -p < infra/mysql-init/01-audit.sql
-- mysql -u root -p < infra/mysql-init/02-care.sql
-- mysql -u root -p < infra/mysql-init/03-ehr.sql

-- Or use docker-compose:
-- docker-compose -f infra/docker-compose.yml up -d mysql

-- ============================================================
-- Database Summary
-- ============================================================

-- audit_db (port 3307)
--   - audit_event         : tamper-evident audit log with hash chain
--   - alert               : security alerts
--   - alert_dedup_key     : deduplication for alerts
--   - integrity_check_run : audit chain verification runs

-- care_db (port 3306)
--   - patients            : patient profiles
--   - doctors             : doctor profiles
--   - doctor_assignments  : patient-doctor relationships
--   - consents            : data access permissions

-- ehr_db (port 3306)
--   - ehr_record_current  : current EHR records (encrypted)
--   - ehr_record_version  : version history
--   - ehr_assignment      : doctor access assignments
--   - ehr_lab_object      : lab reports and files
--   - ehr_audit_log       : local audit trail

-- ============================================================
-- Users and Permissions (create manually or via docker-compose)
-- ============================================================

-- CREATE USER 'audit_user'@'%' IDENTIFIED BY 'audit_pass';
-- GRANT ALL PRIVILEGES ON audit_db.* TO 'audit_user'@'%';

-- CREATE USER 'care_user'@'%' IDENTIFIED BY 'care_pass';
-- GRANT ALL PRIVILEGES ON care_db.* TO 'care_user'@'%';

-- CREATE USER 'ehr_user'@'%' IDENTIFIED BY 'ehr_pass';
-- GRANT ALL PRIVILEGES ON ehr_db.* TO 'ehr_user'@'%';

-- FLUSH PRIVILEGES;
