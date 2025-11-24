package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import lombok.Data;

@Data
public class EvaluacionEventDTO {
    private Long degreeWorkId;
    private String titulo;
    private String observaciones;
    private EnumEstadoDegreeWork estadoAnterior;
    private EnumEstadoDegreeWork estadoNuevo;
    private String evaluadorEmail; 
    
    public EvaluacionEventDTO() {}
    
    public EvaluacionEventDTO(Long degreeWorkId, String titulo, String observaciones, 
                             EnumEstadoDegreeWork estadoAnterior, EnumEstadoDegreeWork estadoNuevo) {
        this.degreeWorkId = degreeWorkId;
        this.titulo = titulo;
        this.observaciones = observaciones;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }
}