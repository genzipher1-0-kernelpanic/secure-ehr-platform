-- ============================================================
-- Create Users and Grant Permissions
-- This runs on the main mysql instance (care_db, ehr_db)
-- The audit_user is created by docker environment variables
-- ============================================================

-- ============================================================
-- Care Service User
-- ============================================================
CREATE USER IF NOT EXISTS 'care_user'@'%' IDENTIFIED BY 'care_pass';
GRANT ALL PRIVILEGES ON care_db.* TO 'care_user'@'%';

-- ============================================================
-- EHR Service User
-- ============================================================
CREATE USER IF NOT EXISTS 'ehr_user'@'%' IDENTIFIED BY 'ehr_pass';
GRANT ALL PRIVILEGES ON ehr_db.* TO 'ehr_user'@'%';

-- ============================================================
-- Apply Changes
-- ============================================================
FLUSH PRIVILEGES;

-- ============================================================
-- Verify Users
-- ============================================================
SELECT User, Host FROM mysql.user WHERE User IN ('care_user', 'ehr_user');
