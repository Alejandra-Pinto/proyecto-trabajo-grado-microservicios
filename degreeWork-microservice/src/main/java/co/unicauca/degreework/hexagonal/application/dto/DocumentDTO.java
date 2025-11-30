package co.unicauca.degreework.hexagonal.application.dto;

import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento;
import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    private EnumTipoDocumento tipo;
    private EnumEstadoDocument estado;
    private String rutaArchivo;
    
    /**
     * @return Integer return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return EnumTipoDocumento return the tipoDocumento
     */
    public EnumTipoDocumento getTipo() {
        return tipo;
    }

    /**
     * @param tipoDocumento the tipoDocumento to set
     */
    public void setTipo(EnumTipoDocumento tipoDocumento) {
        this.tipo = tipoDocumento;
    }

    /**
     * @return EnumEstadoDocument return the estado
     */
    public EnumEstadoDocument getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(EnumEstadoDocument estado) {
        this.estado = estado;
    }

    /**
     * @return String return the rutaArchivo
     */
    public String getRutaArchivo() {
        return rutaArchivo;
    }

    /**
     * @param rutaArchivo the rutaArchivo to set
     */
    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

}
