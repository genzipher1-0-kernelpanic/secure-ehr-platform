package com.team.ehr.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.ehr.entity.EhrCategory;
import jakarta.validation.constraints.NotNull;

public class EhrUpdateRequest {

    @NotNull
    private EhrCategory category;

    @NotNull
    private Integer expectedVersion;

    @NotNull
    private JsonNode patch;

    public EhrCategory getCategory() {
        return category;
    }

    public void setCategory(EhrCategory category) {
        this.category = category;
    }

    public Integer getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Integer expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    public JsonNode getPatch() {
        return patch;
    }

    public void setPatch(JsonNode patch) {
        this.patch = patch;
    }
}
