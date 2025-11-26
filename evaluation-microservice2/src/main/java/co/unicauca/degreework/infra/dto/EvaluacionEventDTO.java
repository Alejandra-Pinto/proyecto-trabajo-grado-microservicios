package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import lombok.Data;

@Data
public class EvaluacionEventDTO {
    private Long degreeWorkId;
    private String titulo;
    private String observaciones;
    private EnumEstadoDocument estadoAnterior;
    private EnumEstadoDocument estadoNuevo;
    private String evaluadorEmail; 
    
    public EvaluacionEventDTO() {}
    
    public EvaluacionEventDTO(Long degreeWorkId, String titulo, String observaciones, 
                             EnumEstadoDocument estadoAnterior, EnumEstadoDocument estadoNuevo) {
        this.degreeWorkId = degreeWorkId;
        this.titulo = titulo;
        this.observaciones = observaciones;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }
}