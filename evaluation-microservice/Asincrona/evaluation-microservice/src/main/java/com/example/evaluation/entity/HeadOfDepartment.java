package com.example.evaluation.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "jefes_departamento")
public class HeadOfDepartment extends User {

    public HeadOfDepartment() {
        super();
        this.setRole("HEAD");
    }

    public HeadOfDepartment(String firstName, String lastName, String phone,
            String program, String email, String password) {
        super(firstName, lastName, phone, program, email, password, "HEAD");
    }

    @Override
    public void showDashboard() {
        System.out.println("Panel del Jefe de Departamento - Gestión de evaluadores y trabajos de grado");
    }

    /**
     * Simula la asignación de evaluadores a un trabajo de grado.
     */
    public void asignarEvaluadores(DegreeWork trabajo, List<Evaluador> evaluadores) {
        System.out.println("Asignando evaluadores al anteproyecto: " + trabajo.getTituloProyecto());
        for (Evaluador e : evaluadores) {
            System.out.println(" - Evaluador asignado: " + e.getNombre() + " (" + e.getRol() + ")");
        }
    }
}
