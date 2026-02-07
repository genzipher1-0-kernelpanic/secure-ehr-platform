package com.team.care.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "consents",
        indexes = {
                @Index(name = "idx_consents_patient_id", columnList = "patient_id"),
                @Index(name = "idx_consents_grantee_user_id", columnList = "grantee_user_id"),
                @Index(name = "idx_consents_valid_window", columnList = "valid_from,valid_to")
        }
)
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "grantee_user_id", nullable = false)
    private Long granteeUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 30)
    private ConsentScope scope;

    @CreationTimestamp
    @Column(name = "valid_from", nullable = false, updatable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getGranteeUserId() {
        return granteeUserId;
    }

    public void setGranteeUserId(Long granteeUserId) {
        this.granteeUserId = granteeUserId;
    }

    public ConsentScope getScope() {
        return scope;
    }

    public void setScope(ConsentScope scope) {
        this.scope = scope;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
