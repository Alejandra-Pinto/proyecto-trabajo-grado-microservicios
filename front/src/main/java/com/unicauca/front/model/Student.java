package com.unicauca.front.model;

public class Student extends User{
    public Student() {
        super();
    }
    
    public Student(String firstName, String lastName, String phone, String program,
                   String email, String password, String role, String status) {
        super(firstName, lastName, phone, program, email, password, "STUDENT", "ACEPTADO");
    }

}
