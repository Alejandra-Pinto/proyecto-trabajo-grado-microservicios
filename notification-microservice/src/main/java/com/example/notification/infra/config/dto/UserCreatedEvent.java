package com.example.notification.infra.config.dto;

import java.io.Serializable;


public class UserCreatedEvent implements Serializable {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String program;
    private String status;
    private boolean isEvaluator; 

    public UserCreatedEvent() {}

    public UserCreatedEvent(Long id, String firstName, String lastName, String email,
                            String role, String program, String status, boolean isEvaluator) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.program = program;
        this.status = status;
        this.isEvaluator = isEvaluator;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isEvaluator() { return isEvaluator; }
    public void setEvaluator(boolean evaluator) { isEvaluator = evaluator; }
}
