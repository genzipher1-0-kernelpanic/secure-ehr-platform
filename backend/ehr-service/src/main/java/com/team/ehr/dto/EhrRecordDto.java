package com.team.ehr.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.ehr.entity.EhrCategory;
import java.time.Instant;

public class EhrRecordDto {

    private EhrCategory category;
    private Integer version;
    private JsonNode data;
    private Instant updatedAt;

    public EhrCategory getCategory() {
        return category;
    }

    public void setCategory(EhrCategory category) {
        this.category = category;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
