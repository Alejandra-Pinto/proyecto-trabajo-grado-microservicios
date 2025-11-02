package com.example.users.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    public Student() {
        super();
    }

    public Student(String firstName, String lastName, String phone, String program,
                   String email, String password) {
        super(firstName, lastName, phone, program, email, password, "STUDENT");
        this.setStatus("ACEPTADO");
    }


    public Student(String fullName, String email, String password) {
        super();
        String[] parts = fullName.split(" ", 2);
        this.setFirstName(parts[0]);
        this.setLastName(parts.length > 1 ? parts[1] : "");
        this.setPhone("0000000");
        this.setProgram("Ingeniería de Sistemas");
        this.setEmail(email);
        this.setPassword(password);
        this.setRole("STUDENT");
        this.setStatus("ACEPTADO");
    }

    @Override
    public void showDashboard() {
        // Lógica específica del panel del estudiante (si se usa en el frontend o lógica interna)
    }

    // Métodos específicos
    public void startThesis() { /* Implementación futura */ }

    public void viewThesisStatus() { /* Implementación futura */ }
}
