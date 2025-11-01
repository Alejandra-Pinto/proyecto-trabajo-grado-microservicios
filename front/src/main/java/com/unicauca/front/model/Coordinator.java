package com.unicauca.front.model;

public class Coordinator extends User{
    public Coordinator() {
        super();
    }
    
    public Coordinator(String firstName, String lastName, String phone, String program,
                       String email, String password, String role, String status) {
        super(firstName, lastName, phone, program, email, password, "COORDINATOR", "PENDIENTE");
    }
}
