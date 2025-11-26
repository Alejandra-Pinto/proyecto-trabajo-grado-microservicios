package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;

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