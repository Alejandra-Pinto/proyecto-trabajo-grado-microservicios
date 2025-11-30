package co.unicauca.degreework.hexagonal.domain.patterns.builder;

import co.unicauca.degreework.hexagonal.domain.model.*;
import co.unicauca.degreework.hexagonal.domain.model.enums.*;
import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import co.unicauca.degreework.hexagonal.application.dto.DocumentDTO;

import java.time.LocalDate;
import java.util.*;

public abstract class DegreeWorkBuilder {

    protected DegreeWork degreeWork;

    public DegreeWorkBuilder() {
        this.degreeWork = new DegreeWork();
        this.degreeWork.setEstudiantes(new ArrayList<>());
        this.degreeWork.setCodirectoresProyecto(new ArrayList<>());
        this.degreeWork.setObjetivosEspecificos(new ArrayList<>());
        this.degreeWork.setFormatosA(new ArrayList<>());
        this.degreeWork.setCartasAceptacion(new ArrayList<>());
        this.degreeWork.setAnteproyectos(new ArrayList<>());
    }

    public abstract void buildModalidad();
    public abstract void buildDocumentosIniciales();
    public abstract DegreeWorkBuilder agregarEstudiante(User estudiante);

    public DegreeWorkBuilder titulo(Titulo titulo) {
        degreeWork.setTitulo(titulo);
        return this;
    }

    public DegreeWorkBuilder director(User director) {
        degreeWork.setDirectorProyecto(director);
        return this;
    }

    public DegreeWorkBuilder agregarCodirector(User codirector) {
        degreeWork.getCodirectoresProyecto().add(codirector);
        return this;
    }

    public DegreeWorkBuilder objetivoGeneral(String objetivoGeneral) {
        degreeWork.setObjetivoGeneral(objetivoGeneral);
        return this;
    }

    public DegreeWorkBuilder objetivosEspecificos(List<String> objetivos) {
        degreeWork.setObjetivosEspecificos(objetivos);
        return this;
    }

    public DegreeWorkBuilder fechaActual(FechaCreacion fecha) {
        degreeWork.setFechaActual(fecha);
        return this;
    }

    public DegreeWorkBuilder estadoInicial(EnumEstadoDegreeWork estado) {
        degreeWork.setEstado(estado);
        return this;
    }

    /**
     * Carga los documentos enviados en el DTO.
     */
    public DegreeWorkBuilder documentosDesdeDTOs(List<DocumentDTO> documentosDTO) {
        if (documentosDTO == null || documentosDTO.isEmpty()) {
            return this;
        }

        for (DocumentDTO docDto : documentosDTO) {

            if (docDto.getTipo() == null) {
                System.out.println("⚠ DOCUMENTO IGNORADO - tipo es NULL: " + docDto);
                continue;
            }

            Document doc = new Document();
            doc.setTipo(docDto.getTipo());
            doc.setEstado(docDto.getEstado());
            doc.setRutaArchivo(docDto.getRutaArchivo());
            doc.setFechaActual(LocalDate.now()); // <-- ESTA ERA LA QUE FALTABA

            switch (docDto.getTipo()) {
                case FORMATO_A:
                    degreeWork.getFormatosA().add(doc);
                    break;
                case CARTA_ACEPTACION:
                    degreeWork.getCartasAceptacion().add(doc);
                    break;
                case ANTEPROYECTO:
                    degreeWork.getAnteproyectos().add(doc);
                    break;
                default:
                    System.out.println("⚠ Tipo de documento desconocido: " + docDto.getTipo());
            }
        }

        return this;
    }



    public DegreeWork build() {
        return degreeWork;
    }
}
