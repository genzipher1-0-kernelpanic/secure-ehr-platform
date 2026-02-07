package com.team.care.dto;

public class AssignmentResponse {

    private Long assignmentId;

    public AssignmentResponse() {
    }

    public AssignmentResponse(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }
}
