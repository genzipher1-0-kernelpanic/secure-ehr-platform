package com.ehrplatform.audit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for integrity verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityVerifyRequest {

    @NotNull(message = "fromId is required")
    @Min(value = 1, message = "fromId must be positive")
    private Long fromId;

    @NotNull(message = "toId is required")
    @Min(value = 1, message = "toId must be positive")
    private Long toId;
}
