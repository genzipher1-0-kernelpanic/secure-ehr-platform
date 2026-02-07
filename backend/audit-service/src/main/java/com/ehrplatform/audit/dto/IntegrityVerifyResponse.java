package com.ehrplatform.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response for integrity verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityVerifyResponse {

    private Long checkRunId;

    private String status;  // OK or FAIL

    private Long fromEventId;
    private Long toEventId;
    private Long lastVerifiedEventId;

    private Long totalEventsChecked;

    private String expectedHash;
    private String foundHash;
    private String failReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant finishedAt;
}
