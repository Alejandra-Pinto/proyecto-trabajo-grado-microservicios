package co.unicauca.degreework.domain.entities.builder;

import co.unicauca.degreework.domain.entities.enums.*;
import co.unicauca.degreework.domain.entities.*;

public class ProfessionalPracticeBuilder extends DegreeWorkBuilder {

    @Override
    public void buildModalidad() {
        degreeWork.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);
    }

    @Override
    public void buildDocumentosIniciales() {
        degreeWork.setEstado(EnumEstadoDegreeWork.FORMATO_A);
    }

    @Override
    public DegreeWorkBuilder agregarEstudiante(User estudiante) {
        if (degreeWork.getEstudiantes().size() >= 1) {
            throw new IllegalStateException("La práctica profesional solo permite un estudiante");
        }
        degreeWork.getEstudiantes().add(estudiante);
        return this;
    }
}
