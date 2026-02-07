package com.team.ehr.dto;

import com.team.ehr.entity.EhrCategory;
import java.time.Instant;

public class EhrVersionDto {

    private Integer version;
    private EhrCategory category;
    private Instant createdAt;
    private String createdByRole;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public EhrCategory getCategory() {
        return category;
    }

    public void setCategory(EhrCategory category) {
        this.category = category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
    }
}
