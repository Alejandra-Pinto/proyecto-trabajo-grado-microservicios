package com.unicauca.front.model;

import java.time.LocalDate;

public class Document {
    private Long id;
    private EnumEstadoDocument tipo;
    private String rutaArchivo;
    private LocalDate fechaActual;
    private EnumEstadoDocument estado;

    // Constructores
    public Document() {}

    public Document(EnumEstadoDocument tipo, String rutaArchivo, EnumEstadoDocument estado) {
        this.tipo = tipo;
        this.rutaArchivo = rutaArchivo;
        this.fechaActual = LocalDate.now();
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EnumEstadoDocument getTipo() { return tipo; }
    public void setTipo(EnumEstadoDocument tipo) { this.tipo = tipo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public LocalDate getFechaActual() { return fechaActual; }
    public void setFechaActual(LocalDate fechaActual) { this.fechaActual = fechaActual; }

    public EnumEstadoDocument getEstado() { return estado; }
    public void setEstado(EnumEstadoDocument estado) { this.estado = estado; }
}