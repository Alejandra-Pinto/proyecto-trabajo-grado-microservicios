package com.unicauca.front.model;

public class Teacher extends User{
    public Teacher() {
        super();
    }
    
    public Teacher(String firstName, String lastName, String phone, String program,
                   String email, String password, String role, String status) {
        super(firstName, lastName, phone, program, email, password, "PROFESSOR", "ACEPTADO");
    }
}
