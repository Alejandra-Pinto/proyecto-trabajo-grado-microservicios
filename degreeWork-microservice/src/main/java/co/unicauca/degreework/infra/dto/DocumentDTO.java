package co.unicauca.degreework.infra.dto;

import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.domain.entities.enums.EnumTipoDocumento;
import lombok.Data;

@Data
public class DocumentDTO {
    private Integer id;
    private String nombre;
    private EnumTipoDocumento tipoDocumento;
    private EnumEstadoDocument estado;
    private String rutaArchivo;
}
