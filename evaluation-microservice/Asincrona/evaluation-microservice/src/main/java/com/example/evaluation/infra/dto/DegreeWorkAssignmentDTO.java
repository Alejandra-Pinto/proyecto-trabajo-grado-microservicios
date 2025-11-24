package com.example.evaluation.infra.dto;

import java.io.Serializable;

/**
 * DTO para publicar en RabbitMQ cuando se asignan evaluadores a un trabajo de
 * grado
 */
public class DegreeWorkAssignmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long degreeWorkId;
    private String titulo;
    private String correoEvaluador1;
    private String nombreEvaluador1;
    private String correoEvaluador2;
    private String nombreEvaluador2;
    private String estado; // Estado del trabajo de grado

    public DegreeWorkAssignmentDTO() {
    }

    public DegreeWorkAssignmentDTO(Long degreeWorkId, String titulo,
            String correoEvaluador1, String nombreEvaluador1,
            String correoEvaluador2, String nombreEvaluador2,
            String estado) {
        this.degreeWorkId = degreeWorkId;
        this.titulo = titulo;
        this.correoEvaluador1 = correoEvaluador1;
        this.nombreEvaluador1 = nombreEvaluador1;
        this.correoEvaluador2 = correoEvaluador2;
        this.nombreEvaluador2 = nombreEvaluador2;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getDegreeWorkId() {
        return degreeWorkId;
    }

    public void setDegreeWorkId(Long degreeWorkId) {
        this.degreeWorkId = degreeWorkId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorreoEvaluador1() {
        return correoEvaluador1;
    }

    public void setCorreoEvaluador1(String correoEvaluador1) {
        this.correoEvaluador1 = correoEvaluador1;
    }

    public String getNombreEvaluador1() {
        return nombreEvaluador1;
    }

    public void setNombreEvaluador1(String nombreEvaluador1) {
        this.nombreEvaluador1 = nombreEvaluador1;
    }

    public String getCorreoEvaluador2() {
        return correoEvaluador2;
    }

    public void setCorreoEvaluador2(String correoEvaluador2) {
        this.correoEvaluador2 = correoEvaluador2;
    }

    public String getNombreEvaluador2() {
        return nombreEvaluador2;
    }

    public void setNombreEvaluador2(String nombreEvaluador2) {
        this.nombreEvaluador2 = nombreEvaluador2;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "DegreeWorkAssignmentDTO{" +
                "degreeWorkId=" + degreeWorkId +
                ", titulo='" + titulo + '\'' +
                ", correoEvaluador1='" + correoEvaluador1 + '\'' +
                ", nombreEvaluador1='" + nombreEvaluador1 + '\'' +
                ", correoEvaluador2='" + correoEvaluador2 + '\'' +
                ", nombreEvaluador2='" + nombreEvaluador2 + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}