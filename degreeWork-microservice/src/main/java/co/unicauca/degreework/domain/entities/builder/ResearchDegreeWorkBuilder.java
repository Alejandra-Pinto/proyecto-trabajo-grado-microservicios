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
        // Si no tiene formatos A, crear o lanzar excepción
        if (degreeWork.getFormatosA() == null || degreeWork.getFormatosA().isEmpty()) {
            Document formatoA = new Document();
            formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            degreeWork.getFormatosA().add(formatoA);
        }
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



