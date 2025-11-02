package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO usado para crear o actualizar trabajos de grado.
 * Incluye la información básica del trabajo, participantes
 * y los documentos asociados (Formatos A, Anteproyectos y Cartas de Aceptación).
 */
@Data
public class DegreeWorkDTO {

    private Long id;

    // Participantes
    private List<Integer> estudiantesIds;
    private Long directorId;
    private List<Integer> codirectoresIds;

    // Información general del trabajo
    private String titulo;
    private EnumModalidad modalidad;
    private LocalDate fechaActual;
    private String objetivoGeneral;
    private List<String> objetivosEspecificos;

    private EnumEstadoDegreeWork estado;
    private String correcciones;

    // Documentos asociados
    private List<DocumentDTO> formatosA;
    private List<DocumentDTO> anteproyectos;
    private List<DocumentDTO> cartasAceptacion;
}
