package com.ehrplatform.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for integrity_check_run table.
 * Records results of hash chain verification runs.
 */
@Entity
@Table(name = "integrity_check_run")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityCheckRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "from_event_id", nullable = false)
    private Long fromEventId;

    @Column(name = "to_event_id", nullable = false)
    private Long toEventId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;  // OK or FAIL

    @Column(name = "last_verified_event_id")
    private Long lastVerifiedEventId;

    @Column(name = "expected_hash", length = 64)
    private String expectedHash;

    @Column(name = "found_hash", length = 64)
    private String foundHash;

    @Column(name = "fail_reason", length = 255)
    private String failReason;
}
