package co.unicauca.degreework.hexagonal.domain.patterns.memento;

import java.util.*;

public class DegreeWorkCaretaker {
    private final List<DegreeWorkMemento> history = new ArrayList<>();

    public void addMemento(DegreeWorkMemento memento) {
        history.add(memento);
    }

    public DegreeWorkMemento getMemento(int index) {
        if (index < 0 || index >= history.size()) {
            throw new IndexOutOfBoundsException("Índice inválido en el historial de mementos.");
        }
        return history.get(index);
    }

    public int getHistorySize() {
        return history.size();
    }
}

