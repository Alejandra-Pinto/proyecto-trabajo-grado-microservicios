package co.unicauca.degreework.hexagonal.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable  // 🔥 Agregar esta anotación
public class Titulo {
    
    @Column(name = "titulo_valor", length = 200)  // 🔥 Especificar nombre único de columna
    private final String valor;
    
    // 🔥 Constructor sin argumentos requerido por JPA
    public Titulo() {
        this.valor = "";
    }
    
    public Titulo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (valor.length() > 200) {
            throw new IllegalArgumentException("El título no puede exceder 200 caracteres");
        }
        this.valor = valor.trim();
    }
    
    public String getValor() { 
        return valor; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Titulo)) return false;
        Titulo titulo = (Titulo) o;
        return Objects.equals(valor, titulo.valor);
    }
    
    @Override
    public int hashCode() { 
        return Objects.hash(valor); 
    }
    
    @Override
    public String toString() { 
        return valor; 
    }

    // Método estático para crear Titulo desde String (maneja nulls)
    public static Titulo createTitulo(String valor) {
        return valor != null ? new Titulo(valor) : null;
    }
}