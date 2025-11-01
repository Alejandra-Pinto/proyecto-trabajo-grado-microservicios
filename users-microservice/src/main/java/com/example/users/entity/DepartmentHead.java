package com.example.users.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DEPARTMENT_HEAD")
public class DepartmentHead extends User {

    public DepartmentHead() {
        super();
    }

    public DepartmentHead(String firstName, String lastName, String phone, String program,
                          String email, String password) {
        super(firstName, lastName, phone, program, email, password, "DEPARTMENT_HEAD");
        this.setStatus("PENDIENTE");
    }

    @Override
    public void showDashboard() {
        // Lógica específica del panel del jefe de departamento
    }

    // Responsabilidades específicas del jefe de departamento
    public void assignEvaluators() {
        // En el futuro, aquí se implementará la lógica para asignar 2 evaluadores a un anteproyecto
    }

    public void viewThesisProposals() {
        // Aquí se implementará la lógica para listar todos los anteproyectos
    }
}
