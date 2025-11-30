package co.unicauca.degreework.hexagonal.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class EvaluacionEventDTO {
    private Long degreeWorkId;
    private List<String> evaluadores;
    
    public EvaluacionEventDTO() {}
    
    public EvaluacionEventDTO(Long degreeWorkId, List<String> evaluadores) {
        this.degreeWorkId = degreeWorkId;
        this.evaluadores = evaluadores;
    }
}