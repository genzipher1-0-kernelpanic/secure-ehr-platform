package com.team.care.dto;

import com.team.care.entity.ConsentScope;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class ConsentCreateRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long granteeUserId;

    @NotNull
    private ConsentScope scope;

    private OffsetDateTime validTo;

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

    public OffsetDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(OffsetDateTime validTo) {
        this.validTo = validTo;
    }
}
