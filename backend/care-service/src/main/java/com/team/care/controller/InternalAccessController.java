package com.team.care.controller;

import com.team.care.dto.AccessDecisionResponse;
import com.team.care.entity.ConsentScope;
import com.team.care.service.AccessDecisionService;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/internal/access")
public class InternalAccessController {

    private final AccessDecisionService accessDecisionService;

    public InternalAccessController(AccessDecisionService accessDecisionService) {
        this.accessDecisionService = accessDecisionService;
    }

    @GetMapping
    public AccessDecisionResponse checkAccess(
            @RequestParam @NotNull Long doctorUserId,
            @RequestParam @NotNull Long patientId,
            @RequestParam @NotNull ConsentScope scope
    ) {
        return accessDecisionService.checkAccess(doctorUserId, patientId, scope);
    }
}
