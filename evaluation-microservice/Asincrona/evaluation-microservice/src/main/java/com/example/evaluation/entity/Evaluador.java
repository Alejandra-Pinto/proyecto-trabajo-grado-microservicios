package com.example.evaluation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluador", uniqueConstraints = {
        @UniqueConstraint(columnNames = "correo", name = "uk_evaluador_correo")
})
public class Evaluador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String rol; // "Profesor", "Coordinador", "Director", "Codirector"

    @Column(unique = true, nullable = false)
    private String correo;

    public Evaluador() {
    }

    public Evaluador(String nombre, String rol, String correo) {
        this.nombre = nombre;
        this.rol = rol;
        this.correo = correo;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}