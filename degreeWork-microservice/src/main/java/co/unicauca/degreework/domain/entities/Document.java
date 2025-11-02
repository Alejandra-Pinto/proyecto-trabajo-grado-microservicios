package co.unicauca.degreework.domain.entities;

import co.unicauca.degreework.domain.entities.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EnumTipoDocumento tipo;

    private String rutaArchivo;

    private java.time.LocalDate fechaActual;

    @Enumerated(EnumType.STRING)
    private EnumEstadoDocument estado;
}
