package com.team.ehr.controller;

import com.team.ehr.dto.AssignmentSyncRequest;
import com.team.ehr.service.AssignmentSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/assignments")
public class InternalAssignmentController {

    private final AssignmentSyncService assignmentSyncService;

    public InternalAssignmentController(AssignmentSyncService assignmentSyncService) {
        this.assignmentSyncService = assignmentSyncService;
    }

    @PostMapping
    public ResponseEntity<Void> sync(@Valid @RequestBody AssignmentSyncRequest request) {
        assignmentSyncService.sync(request);
        return ResponseEntity.ok().build();
    }
}
