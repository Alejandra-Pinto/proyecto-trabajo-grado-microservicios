package com.example.users.infra.dto;

public class EvaluatorAssignmentDTO {
    private Long departmentHeadId;
    private Long userId; // Usuario al que se le asigna el rol de evaluador

    // Getters y Setters
    public Long getDepartmentHeadId() {
        return departmentHeadId;
    }

    public void setDepartmentHeadId(Long departmentHeadId) {
        this.departmentHeadId = departmentHeadId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
