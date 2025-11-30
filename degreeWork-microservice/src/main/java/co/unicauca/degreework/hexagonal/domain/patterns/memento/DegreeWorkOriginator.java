package co.unicauca.degreework.hexagonal.domain.patterns.memento;

import co.unicauca.degreework.hexagonal.domain.model.*;

public class DegreeWorkOriginator {
    private DegreeWork degreeWork;

    public DegreeWorkOriginator(DegreeWork degreeWork) {
        this.degreeWork = degreeWork;
    }

    public DegreeWorkMemento save() {
        return new DegreeWorkMemento(
            degreeWork.getTitulo(),
            degreeWork.getEstado(),
            degreeWork.getFechaActual(),
            degreeWork.getCorrecciones()
        );
    }

    public void restore(DegreeWorkMemento memento) {
        degreeWork.setTitulo(memento.getTitulo());
        degreeWork.setEstado(memento.getEstado());
        degreeWork.setFechaActual(memento.getFecha());
        degreeWork.setCorrecciones(memento.getCorrecciones());
    }

    public DegreeWork getDegreeWork() {
        return degreeWork;
    }
}

