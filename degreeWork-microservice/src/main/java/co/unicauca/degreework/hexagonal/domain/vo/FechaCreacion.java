package co.unicauca.degreework.hexagonal.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable  // 🔥 Agregar esta anotación
public class FechaCreacion {
    
    @Column(name = "fecha_creacion")  // 🔥 Especificar nombre único de columna
    private final LocalDate valor;
    
    // 🔥 Constructor sin argumentos requerido por JPA
    public FechaCreacion() {
        this.valor = LocalDate.now();
    }
    
    public FechaCreacion(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura");
        }
        this.valor = fecha;
    }
    
    public LocalDate getValor() { 
        return valor; 
    }
    
    // 🔥 Método getter con nombre diferente para evitar conflictos
    public LocalDate getFecha() {
        return valor;
    }
    
    public boolean esReciente() {
        return valor.isAfter(LocalDate.now().minusMonths(1));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FechaCreacion)) return false;
        FechaCreacion that = (FechaCreacion) o;
        return Objects.equals(valor, that.valor);
    }
    
    @Override
    public int hashCode() { 
        return Objects.hash(valor); 
    }
    
    @Override
    public String toString() { 
        return valor.toString(); 
    }

    // Método estático para crear FechaCreacion desde LocalDate
    public static FechaCreacion createFechaCreacion(LocalDate fecha) {
        return fecha != null ? new FechaCreacion(fecha) : null;
    }
}