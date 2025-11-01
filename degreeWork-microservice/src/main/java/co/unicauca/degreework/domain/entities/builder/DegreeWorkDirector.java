package co.unicauca.degreework.domain.entities.builder;

import co.unicauca.degreework.domain.entities.DegreeWork;

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

