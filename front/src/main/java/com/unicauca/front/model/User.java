package com.unicauca.front.model;

public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String program;
    private String email;
    private String password;
    private String role;
    private String status;

    public User() {}

    public User(String firstName, String lastName, String phone, String program,
                String email, String password, String role, String status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.program = program;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    // GETTER Y SETTER PARA ID
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
}
