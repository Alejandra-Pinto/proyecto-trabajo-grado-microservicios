package com.example.users.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEACHER")
public class Teacher extends User {

    public Teacher() {
        super();
    }

    public Teacher(String firstName, String lastName, String phone, String program,
                   String email, String password) {
        super(firstName, lastName, phone, program, email, password, "TEACHER");
        this.setStatus("ACEPTADO");
    }

    //Constructor auxiliar más simple (para DataLoader)
    public Teacher(String fullName, String email) {
        super();
        String[] parts = fullName.split(" ", 2);
        this.setFirstName(parts[0]);
        this.setLastName(parts.length > 1 ? parts[1] : "");
        this.setProgram("Desconocido");
        this.setEmail(email);
        this.setPassword("default123");
        this.setRole("TEACHER");
        this.setStatus("ACEPTADO");
    }

    @Override
    public void showDashboard() {
        // Lógica específica del panel del docente
    }

    // Ejemplo de posibles responsabilidades
    public void evaluateThesis() { /* Implementación futura */ }

    public void reviewProject() { /* Implementación futura */ }
}
