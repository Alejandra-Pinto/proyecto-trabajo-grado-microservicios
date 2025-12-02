package com.unicauca.front.dto;

import com.unicauca.front.model.EnumEstadoDocument;

public class ActualizarEvaluacionDTO {
    private Long degreeWorkId;
    private String observaciones;
    private EnumEstadoDocument estado;
    
    // Getters y Setters
    public Long getDegreeWorkId() {
        return degreeWorkId;
    }
    
    public void setDegreeWorkId(Long degreeWorkId) {
        this.degreeWorkId = degreeWorkId;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public EnumEstadoDocument getEstado() {
        return estado;
    }
    
    public void setEstado(EnumEstadoDocument estado) {
        this.estado = estado;
    }
}