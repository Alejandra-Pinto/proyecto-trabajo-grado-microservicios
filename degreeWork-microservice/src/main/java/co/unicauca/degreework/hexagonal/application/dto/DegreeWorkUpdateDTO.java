package co.unicauca.degreework.hexagonal.application.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class DegreeWorkUpdateDTO implements Serializable {

    private Integer degreeWorkId; // ID del trabajo de grado
    private String estado; // Nuevo estado (EnumEstadoDegreeWork en texto)
    private String correcciones; // Texto con las observaciones o correcciones
    
    public DegreeWorkUpdateDTO() {}
}
