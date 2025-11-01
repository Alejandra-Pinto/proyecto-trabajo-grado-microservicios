package co.unicauca.degreework.domain.entities.builder;

import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.enums.*;
import java.time.LocalDate;
import java.util.*;

public abstract class DegreeWorkBuilder {

    protected DegreeWork degreeWork;

    public DegreeWorkBuilder() {
        this.degreeWork = new DegreeWork();
        this.degreeWork.setEstudiantes(new ArrayList<>());
        this.degreeWork.setCodirectoresProyecto(new ArrayList<>());
        this.degreeWork.setObjetivosEspecificos(new ArrayList<>());
    }

    public abstract void buildModalidad();
    public abstract void buildDocumentosIniciales();
    public abstract DegreeWorkBuilder agregarEstudiante(User estudiante);

    public DegreeWorkBuilder titulo(String titulo) {
        degreeWork.setTitulo(titulo);
        return this;
    }

    public DegreeWorkBuilder director(User director) {
        degreeWork.setDirectorProyecto(director);
        return this;
    }

    public DegreeWorkBuilder agregarCodirector(User codirector) {
        degreeWork.getCodirectoresProyecto().add(codirector);
        return this;
    }

    public DegreeWorkBuilder objetivoGeneral(String objetivoGeneral) {
        degreeWork.setObjetivoGeneral(objetivoGeneral);
        return this;
    }

    public DegreeWorkBuilder objetivosEspecificos(List<String> objetivos) {
        degreeWork.setObjetivosEspecificos(objetivos);
        return this;
    }

    public DegreeWorkBuilder fechaActual(LocalDate fecha) {
        degreeWork.setFechaActual(fecha);
        return this;
    }

    public DegreeWorkBuilder estadoInicial(EnumEstadoDegreeWork estado) {
        degreeWork.setEstado(estado);
        return this;
    }

    public DegreeWork build() {
        return degreeWork;
    }
}
