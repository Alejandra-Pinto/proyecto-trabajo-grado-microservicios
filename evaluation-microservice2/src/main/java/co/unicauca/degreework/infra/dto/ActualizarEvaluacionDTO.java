package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;

public class ActualizarEvaluacionDTO {
    private Long degreeWorkId;
    private String observaciones;
    private EnumEstadoDegreeWork estado;
    
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
    
    public EnumEstadoDegreeWork getEstado() {
        return estado;
    }
    
    public void setEstado(EnumEstadoDegreeWork estado) {
        this.estado = estado;
    }
}