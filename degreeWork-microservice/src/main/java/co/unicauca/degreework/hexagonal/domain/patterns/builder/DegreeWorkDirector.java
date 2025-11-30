package co.unicauca.degreework.hexagonal.domain.patterns.builder;

import co.unicauca.degreework.hexagonal.domain.model.*;

public class DegreeWorkDirector {

    private DegreeWorkBuilder builder;

    public void setBuilder(DegreeWorkBuilder builder) {
        this.builder = builder;
    }

    public DegreeWork construirTrabajo() {
        builder.buildModalidad();
        builder.buildDocumentosIniciales();
        return builder.build();
    }
}

