CREATE DATABASE IF NOT EXISTS audit_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE audit_db;

CREATE TABLE IF NOT EXISTS audit_event (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           occurred_at DATETIME(3) NOT NULL,              -- event time (UTC)
                                           received_at DATETIME(3) NOT NULL,              -- when audit-service ingested it (UTC)

    -- Source
                                           source_service VARCHAR(64) NOT NULL,           -- gateway / identity / ehr
                                           source_instance VARCHAR(128) NULL,             -- optional: instanceId/hostname

    -- Event classification
                                           event_type VARCHAR(64) NOT NULL,               -- e.g. LOGIN_FAILURE, RECORD_VIEWED
                                           outcome VARCHAR(16) NOT NULL,                  -- SUCCESS | FAILURE | DENIED
                                           severity VARCHAR(16) NOT NULL DEFAULT 'INFO',  -- INFO | WARN | HIGH | CRITICAL

    -- Identity/context
                                           actor_user_id BIGINT NULL,
                                           actor_role VARCHAR(32) NULL,                   -- PATIENT/DOCTOR/ADMIN/SYS_ADMIN etc
                                           actor_email VARCHAR(255) NULL,                 -- for login failures where no userId yet

                                           ip VARCHAR(64) NULL,
                                           user_agent VARCHAR(512) NULL,
                                           device_id VARCHAR(128) NULL,                   -- optional
                                           session_id VARCHAR(128) NULL,                  -- optional

    -- Domain references (healthcare)
                                           patient_id BIGINT NULL,
                                           record_id BIGINT NULL,
                                           target_user_id BIGINT NULL,                    -- e.g. admin created user / role changed

    -- Request correlation
                                           request_id VARCHAR(64) NULL,                   -- idempotency key from producer
                                           trace_id VARCHAR(64) NULL,                     -- Zipkin later
                                           span_id VARCHAR(64) NULL,

    -- Extra metadata (sanitized, never PHI text)
                                           details_json JSON NULL,

    -- Tamper-evident chain
                                           prev_hash VARCHAR(64) NOT NULL,
                                           event_hash VARCHAR(64) NOT NULL,

                                           PRIMARY KEY (id),

    -- idempotency: allows safe retries from Kafka consumers
                                           UNIQUE KEY uq_audit_request (request_id),

                                           KEY idx_occurred_at (occurred_at),
                                           KEY idx_actor_time (actor_user_id, occurred_at),
                                           KEY idx_actor_email_time (actor_email, occurred_at),
                                           KEY idx_patient_time (patient_id, occurred_at),
                                           KEY idx_record_time (record_id, occurred_at),
                                           KEY idx_event_time (event_type, occurred_at),
                                           KEY idx_outcome_time (outcome, occurred_at),
                                           KEY idx_service_time (source_service, occurred_at)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS alert (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     created_at DATETIME(3) NOT NULL,
                                     updated_at DATETIME(3) NOT NULL,

                                     alert_type VARCHAR(64) NOT NULL,              -- REPEATED_FAILED_LOGIN, ...
                                     severity VARCHAR(16) NOT NULL,                -- MED | HIGH | CRITICAL etc
                                     status VARCHAR(16) NOT NULL DEFAULT 'OPEN',   -- OPEN | ACKED | RESOLVED

                                     title VARCHAR(160) NOT NULL,
                                     message VARCHAR(1024) NOT NULL,

    -- optional links for triage
                                     actor_user_id BIGINT NULL,
                                     actor_email VARCHAR(255) NULL,
                                     ip VARCHAR(64) NULL,
                                     patient_id BIGINT NULL,

    -- evidence (counts, sample event IDs, time window)
                                     evidence_json JSON NULL,

                                     PRIMARY KEY (id),
                                     KEY idx_status_sev (status, severity, created_at),
                                     KEY idx_type_time (alert_type, created_at),
                                     KEY idx_actor_time (actor_user_id, created_at),
                                     KEY idx_actor_email_time (actor_email, created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alert_dedup_key (
                                               dedup_key VARCHAR(128) NOT NULL,         -- e.g. "REPEATED_FAILED_LOGIN:email:ip:2026-02-07T11:20"
                                               alert_id BIGINT NOT NULL,
                                               expires_at DATETIME(3) NOT NULL,

                                               PRIMARY KEY (dedup_key),
                                               KEY idx_expires (expires_at),
                                               CONSTRAINT fk_dedup_alert FOREIGN KEY (alert_id) REFERENCES alert(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS integrity_check_run (
                                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                                   started_at DATETIME(3) NOT NULL,
                                                   finished_at DATETIME(3) NULL,

                                                   from_event_id BIGINT NOT NULL,
                                                   to_event_id BIGINT NOT NULL,

                                                   status VARCHAR(16) NOT NULL,               -- OK | FAIL
                                                   last_verified_event_id BIGINT NULL,

                                                   expected_hash VARCHAR(64) NULL,
                                                   found_hash VARCHAR(64) NULL,
                                                   fail_reason VARCHAR(255) NULL,

                                                   PRIMARY KEY (id),
                                                   KEY idx_time (started_at),
                                                   KEY idx_status_time (status, started_at)
) ENGINE=InnoDB;
