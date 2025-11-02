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
        // Si no tiene formatos A, crear o lanzar excepción
        if (degreeWork.getFormatosA() == null || degreeWork.getFormatosA().isEmpty()) {
            Document formatoA = new Document();
            formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            degreeWork.getFormatosA().add(formatoA);
        }

        if (degreeWork.getCartasAceptacion() == null || degreeWork.getCartasAceptacion().isEmpty()) {
            Document cartaAceptacion = new Document();
            cartaAceptacion.setTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            cartaAceptacion.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
            degreeWork.getCartasAceptacion().add(cartaAceptacion);
        }

        // Si no tiene carta de aceptación, crear un placeholder o lanzar excepción
        if (degreeWork.getCartasAceptacion() == null || degreeWork.getCartasAceptacion().isEmpty()) {
            throw new IllegalArgumentException("Debe subir al menos una Carta de Aceptación para la práctica profesional.");
        }

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
