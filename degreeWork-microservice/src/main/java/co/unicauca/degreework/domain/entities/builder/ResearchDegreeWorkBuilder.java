package co.unicauca.degreework.domain.entities.builder;

import co.unicauca.degreework.domain.entities.enums.*;
import co.unicauca.degreework.domain.entities.*;

public class ResearchDegreeWorkBuilder extends DegreeWorkBuilder {

    @Override
    public void buildModalidad() {
        degreeWork.setModalidad(EnumModalidad.INVESTIGACION);
    }

    @Override
    public void buildDocumentosIniciales() {
        degreeWork.setEstado(EnumEstadoDegreeWork.FORMATO_A);
    }

    @Override
    public DegreeWorkBuilder agregarEstudiante(User estudiante) {
        if (degreeWork.getEstudiantes().size() >= 2) {
            throw new IllegalStateException("La modalidad de investigacion solo permite dos estudiante");
        }
        degreeWork.getEstudiantes().add(estudiante);
        return this;
    }
}



