package com.team.ehr.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.ehr.entity.EhrCategory;
import jakarta.validation.constraints.NotNull;

public class EhrCreateRequest {

    @NotNull
    private EhrCategory category;

    @NotNull
    private JsonNode data;

    public EhrCategory getCategory() {
        return category;
    }

    public void setCategory(EhrCategory category) {
        this.category = category;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
