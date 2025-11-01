package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DegreeWorkDTO {
    private int id;
    private List<Integer> estudiantesIds;
    private int directorId;
    private List<Integer> codirectoresIds;

    private String titulo;
    private EnumModalidad modalidad;
    private LocalDate fechaActual;
    private String objetivoGeneral;
    private List<String> objetivosEspecificos;

    private EnumEstadoDegreeWork estado;
    private String correcciones;
}
