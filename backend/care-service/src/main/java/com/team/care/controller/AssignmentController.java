package com.team.care.controller;

import com.team.care.dto.AssignmentCreateRequest;
import com.team.care.dto.AssignmentResponse;
import com.team.care.service.AssignmentService;
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
@RequestMapping("/api/care/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody AssignmentCreateRequest request
    ) {
        Long id = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AssignmentResponse(id));
    }

    @PutMapping("/{assignmentId}/end")
    public ResponseEntity<AssignmentResponse> endAssignment(@PathVariable Long assignmentId) {
        assignmentService.endAssignment(assignmentId);
        return ResponseEntity.ok(new AssignmentResponse(assignmentId));
    }
}
