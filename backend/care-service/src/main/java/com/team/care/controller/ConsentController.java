package com.team.care.controller;

import com.team.care.dto.ConsentCreateRequest;
import com.team.care.dto.ConsentResponse;
import com.team.care.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/care/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping
    public ResponseEntity<ConsentResponse> createConsent(
            @Valid @RequestBody ConsentCreateRequest request
    ) {
        Long id = consentService.createConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConsentResponse(id));
    }

    @PutMapping("/{consentId}/revoke")
    public ResponseEntity<ConsentResponse> revokeConsent(@PathVariable Long consentId) {
        consentService.revokeConsent(consentId);
        return ResponseEntity.ok(new ConsentResponse(consentId));
    }
}
