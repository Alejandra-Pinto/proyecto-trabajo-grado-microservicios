package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO usado para crear o actualizar trabajos de grado.
 * Incluye la información básica del trabajo, participantes
 * y los documentos asociados (Formatos A, Anteproyectos y Cartas de Aceptación).
 */
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Integer> getEstudiantesIds() {
        return estudiantesIds;
    }

    public void setEstudiantesIds(List<Integer> estudiantesIds) {
        this.estudiantesIds = estudiantesIds;
    }

    public Long getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Long directorId) {
        this.directorId = directorId;
    }

    public List<Integer> getCodirectoresIds() {
        return codirectoresIds;
    }

    public void setCodirectoresIds(List<Integer> codirectoresIds) {
        this.codirectoresIds = codirectoresIds;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public EnumModalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(EnumModalidad modalidad) {
        this.modalidad = modalidad;
    }

    public LocalDate getFechaActual() {
        return fechaActual;
    }

    public void setFechaActual(LocalDate fechaActual) {
        this.fechaActual = fechaActual;
    }

    public String getObjetivoGeneral() {
        return objetivoGeneral;
    }

    public void setObjetivoGeneral(String objetivoGeneral) {
        this.objetivoGeneral = objetivoGeneral;
    }

    public List<String> getObjetivosEspecificos() {
        return objetivosEspecificos;
    }

    public void setObjetivosEspecificos(List<String> objetivosEspecificos) {
        this.objetivosEspecificos = objetivosEspecificos;
    }

    public EnumEstadoDegreeWork getEstado() {
        return estado;
    }

    public void setEstado(EnumEstadoDegreeWork estado) {
        this.estado = estado;
    }

    public String getCorrecciones() {
        return correcciones;
    }

    public void setCorrecciones(String correcciones) {
        this.correcciones = correcciones;
    }

    public List<DocumentDTO> getFormatosA() {
        return formatosA;
    }

    public void setFormatosA(List<DocumentDTO> formatosA) {
        this.formatosA = formatosA;
    }

    public List<DocumentDTO> getAnteproyectos() {
        return anteproyectos;
    }

    public void setAnteproyectos(List<DocumentDTO> anteproyectos) {
        this.anteproyectos = anteproyectos;
    }

    public List<DocumentDTO> getCartasAceptacion() {
        return cartasAceptacion;
    }

    public void setCartasAceptacion(List<DocumentDTO> cartasAceptacion) {
        this.cartasAceptacion = cartasAceptacion;
    }
}