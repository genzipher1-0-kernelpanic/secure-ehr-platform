-- ============================================
-- Identity Service - Initial Schema
-- Flyway Version: V1
-- ============================================

-- NOTE:
-- Database (identity_db) is assumed to exist.
-- Flyway manages schema, not database creation.

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       email           VARCHAR(320) NOT NULL,
                       password_hash   VARCHAR(255) NOT NULL,
                       status          ENUM('ACTIVE', 'LOCKED', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
                       id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                       name ENUM(
        'SUPER_ADMIN',
        'SYSTEM_ADMIN',
        'ADMIN',
        'DOCTOR',
        'PATIENT'
    ) NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =========================
-- USER ↔ ROLE MAPPING
-- =========================
CREATE TABLE user_roles (
                            user_id BIGINT UNSIGNED NOT NULL,
                            role_id BIGINT UNSIGNED NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =========================
-- TOKENS (ACCESS + REFRESH IN SAME ROW)
-- =========================
CREATE TABLE tokens (
                        id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                        user_id             BIGINT UNSIGNED NOT NULL,

                        access_token_hash   VARBINARY(64) NOT NULL,
                        refresh_token_hash  VARBINARY(64) NOT NULL,

                        issued_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        access_expires_at   TIMESTAMP NOT NULL,
                        refresh_expires_at  TIMESTAMP NOT NULL,

                        access_revoked_at   TIMESTAMP NULL,
                        refresh_revoked_at  TIMESTAMP NULL,

                        PRIMARY KEY (id),
                        KEY idx_tokens_user (user_id),
                        KEY idx_tokens_access_exp (access_expires_at),
                        KEY idx_tokens_refresh_exp (refresh_expires_at),

                        CONSTRAINT fk_tokens_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(id)
                                ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================
-- INITIAL ROLE SEED DATA
-- =========================
INSERT INTO roles (name)
VALUES
    ('SUPER_ADMIN'),
    ('SYSTEM_ADMIN'),
    ('ADMIN'),
    ('DOCTOR'),
    ('PATIENT');
