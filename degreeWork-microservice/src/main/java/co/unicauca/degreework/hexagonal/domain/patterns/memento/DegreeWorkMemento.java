package co.unicauca.degreework.hexagonal.domain.patterns.memento;

import co.unicauca.degreework.hexagonal.domain.model.enums.*;
import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;

import java.time.LocalDate;

public class DegreeWorkMemento {
    private final Titulo titulo;
    private final EnumEstadoDegreeWork estado;
    private final FechaCreacion fecha;
    private final String correcciones;

    public DegreeWorkMemento(Titulo titulo, EnumEstadoDegreeWork estado, FechaCreacion fecha, String correcciones) {
        this.titulo = titulo;
        this.estado = estado;
        this.fecha = fecha;
        this.correcciones = correcciones;
    }

    public Titulo getTitulo() { return titulo; }
    public EnumEstadoDegreeWork getEstado() { return estado; }
    public FechaCreacion getFecha() { return fecha; }
    public String getCorrecciones() { return correcciones; }
}


