package co.unicauca.degreework.hexagonal.application.dto;

import java.io.Serializable;

public class DegreeWorkUpdateDTO implements Serializable {
    private Long degreeWorkId;
    private String estado;
    private String correcciones;
    
    // Constructores
    public DegreeWorkUpdateDTO() {}
    
    public DegreeWorkUpdateDTO(Long degreeWorkId, String estado, String correcciones) {
        this.degreeWorkId = degreeWorkId;
        this.estado = estado;
        this.correcciones = correcciones;
    }
    
    // Getters y Setters
    public Long getDegreeWorkId() { return degreeWorkId; }
    public void setDegreeWorkId(Long degreeWorkId) { this.degreeWorkId = degreeWorkId; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getCorrecciones() { return correcciones; }
    public void setCorrecciones(String correcciones) { this.correcciones = correcciones; }
    
    @Override
    public String toString() {
        return "DegreeWorkUpdateDTO(degreeWorkId=" + degreeWorkId + 
               ", estado=" + estado + 
               ", correcciones=" + correcciones + ")";
    }
}