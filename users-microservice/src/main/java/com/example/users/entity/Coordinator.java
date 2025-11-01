package com.example.users.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("COORDINATOR")
public class Coordinator extends User {

    public Coordinator() {
        super();
    }

    public Coordinator(String firstName, String lastName, String phone, String program,
                       String email, String password) {
        super(firstName, lastName, phone, program, email, password, "COORDINATOR");
        this.setStatus("PENDIENTE");
    }

    @Override
    public void showDashboard() {
        // Lógica específica del panel del coordinador
    }

    // Ejemplo de responsabilidades
    public void reviewFormatA() { /* Implementación futura */ }

    public void manageStudents() { /* Implementación futura */ }
}
