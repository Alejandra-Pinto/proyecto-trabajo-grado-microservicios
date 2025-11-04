package com.example.evaluation.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DegreeWorkUpdateDTO {

    private Long degreeWorkId; // ID del trabajo de grado
    private String estado; // Nuevo estado (EnumEstadoDegreeWork en texto)
    private String correcciones; // Texto con las observaciones o correcciones
}
