package co.unicauca.degreework.domain.entities.memento;

import co.unicauca.degreework.domain.entities.enums.*;
import java.time.LocalDate;

public class DegreeWorkMemento {
    private final String titulo;
    private final EnumEstadoDegreeWork estado;
    private final LocalDate fecha;
    private final String correcciones;

    public DegreeWorkMemento(String titulo, EnumEstadoDegreeWork estado, LocalDate fecha, String correcciones) {
        this.titulo = titulo;
        this.estado = estado;
        this.fecha = fecha;
        this.correcciones = correcciones;
    }

    public String getTitulo() { return titulo; }
    public EnumEstadoDegreeWork getEstado() { return estado; }
    public LocalDate getFecha() { return fecha; }
    public String getCorrecciones() { return correcciones; }
}


