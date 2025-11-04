package com.example.evaluation.entity;

import com.example.evaluation.entity.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnumTipoDocumento tipo;

    private String rutaArchivo;

    private LocalDate fechaActual;

    @Enumerated(EnumType.STRING)
    private EnumEstadoDocument estado;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnumTipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(EnumTipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDate getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(LocalDate fechaActual) {
        this.fechaActual = fechaActual;
    }

    public EnumEstadoDocument getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoDocument estado) {
        this.estado = estado;
    }
}