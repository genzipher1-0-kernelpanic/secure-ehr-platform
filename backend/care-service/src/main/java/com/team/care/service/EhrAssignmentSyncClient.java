package com.team.care.service;

import com.team.care.dto.EhrAssignmentSyncRequest;
import com.team.care.service.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class EhrAssignmentSyncClient {

    private static final String INTERNAL_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String internalToken;

    public EhrAssignmentSyncClient(@Value("${ehr.service.baseUrl}") String baseUrl,
                                   @Value("${ehr.service.internalToken:}") String internalToken) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.internalToken = internalToken;
    }

    public void assign(Long patientId, Long doctorUserId) {
        sync(patientId, doctorUserId, EhrAssignmentSyncRequest.Action.ASSIGN);
    }

    public void end(Long patientId, Long doctorUserId) {
        sync(patientId, doctorUserId, EhrAssignmentSyncRequest.Action.END);
    }

    private void sync(Long patientId, Long doctorUserId, EhrAssignmentSyncRequest.Action action) {
        if (internalToken == null || internalToken.isBlank()) {
            throw new ExternalServiceException("EHR internal token is not configured");
        }

        EhrAssignmentSyncRequest payload = new EhrAssignmentSyncRequest();
        payload.setPatientId(patientId);
        payload.setDoctorUserId(doctorUserId);
        payload.setAction(action);

        try {
            restClient.post()
                    .uri("/internal/assignments")
                    .header(INTERNAL_HEADER, internalToken)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            String message = "EHR sync failed with status " + ex.getRawStatusCode();
            String body = ex.getResponseBodyAsString();
            if (body != null && !body.isBlank()) {
                message += ": " + body;
            }
            throw new ExternalServiceException(message, ex);
        } catch (ResourceAccessException ex) {
            throw new ExternalServiceException("EHR sync failed: " + ex.getMessage(), ex);
        }
    }
}
