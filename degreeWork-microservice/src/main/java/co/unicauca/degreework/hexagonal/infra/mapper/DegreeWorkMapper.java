package co.unicauca.degreework.hexagonal.infra.mapper;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.DocumentDTO;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DegreeWorkMapper {

    // Convertir de Entity a DTO
    public DegreeWorkDTO toDTO(DegreeWork degreeWork) {
        if (degreeWork == null) {
            return null;
        }

        DegreeWorkDTO dto = new DegreeWorkDTO();
        dto.setId(degreeWork.getId());
        
        // Convertir Value Objects a tipos primitivos
        dto.setTitulo(degreeWork.getTitulo() != null ? degreeWork.getTitulo().getValor() : null);
        dto.setFechaActual(degreeWork.getFechaActual() != null ? degreeWork.getFechaActual().getValor() : null);
        
        // Convertir participantes a emails
        dto.setEstudiantesEmails(extractEmails(degreeWork.getEstudiantes()));
        dto.setDirectorEmail(degreeWork.getDirectorProyecto() != null ? degreeWork.getDirectorProyecto().getEmail() : null);
        dto.setCodirectoresEmails(extractEmails(degreeWork.getCodirectoresProyecto()));
        
        // Campos simples
        dto.setModalidad(degreeWork.getModalidad());
        dto.setObjetivoGeneral(degreeWork.getObjetivoGeneral());
        dto.setObjetivosEspecificos(degreeWork.getObjetivosEspecificos());
        dto.setEstado(degreeWork.getEstado());
        dto.setCorrecciones(degreeWork.getCorrecciones());
        
        // Convertir documentos
        dto.setFormatosA(toDocumentDTOs(degreeWork.getFormatosA()));
        dto.setAnteproyectos(toDocumentDTOs(degreeWork.getAnteproyectos()));
        dto.setCartasAceptacion(toDocumentDTOs(degreeWork.getCartasAceptacion()));
        
        return dto;
    }

    // Convertir de DTO a Entity (para creación)
    public DegreeWork toEntity(DegreeWorkDTO dto) {
        if (dto == null) {
            return null;
        }

        return DegreeWork.builder()
                .id(dto.getId())
                // Crear Value Objects desde tipos primitivos
                .titulo(dto.getTitulo() != null ? new Titulo(dto.getTitulo()) : null)
                .fechaActual(dto.getFechaActual() != null ? new FechaCreacion(dto.getFechaActual()) : null)
                // Campos simples
                .modalidad(dto.getModalidad())
                .objetivoGeneral(dto.getObjetivoGeneral())
                .objetivosEspecificos(dto.getObjetivosEspecificos())
                .estado(dto.getEstado())
                .correcciones(dto.getCorrecciones())
                // Los participantes se asignan después mediante sus emails
                .estudiantes(new ArrayList<>())
                .codirectoresProyecto(new ArrayList<>())
                .build();
    }

    // Actualizar Entity existente desde DTO
    public void updateEntityFromDTO(DegreeWorkDTO dto, DegreeWork degreeWork) {
        if (dto == null || degreeWork == null) {
            return;
        }

        // Actualizar Value Objects si se proporcionan nuevos valores
        if (dto.getTitulo() != null) {
            degreeWork.setTitulo(new Titulo(dto.getTitulo()));
        }
        if (dto.getFechaActual() != null) {
            degreeWork.setFechaActual(new FechaCreacion(dto.getFechaActual()));
        }

        // Actualizar campos simples
        if (dto.getModalidad() != null) {
            degreeWork.setModalidad(dto.getModalidad());
        }
        if (dto.getObjetivoGeneral() != null) {
            degreeWork.setObjetivoGeneral(dto.getObjetivoGeneral());
        }
        if (dto.getObjetivosEspecificos() != null) {
            degreeWork.setObjetivosEspecificos(dto.getObjetivosEspecificos());
        }
        if (dto.getEstado() != null) {
            degreeWork.setEstado(dto.getEstado());
        }
        if (dto.getCorrecciones() != null) {
            degreeWork.setCorrecciones(dto.getCorrecciones());
        }
    }

    // Métodos auxiliares privados
    private List<String> extractEmails(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        return users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    private List<DocumentDTO> toDocumentDTOs(List<Document> documents) {
        if (documents == null) {
            return new ArrayList<>();
        }
        return documents.stream()
                .map(this::toDocumentDTO)
                .collect(Collectors.toList());
    }

    private DocumentDTO toDocumentDTO(Document document) {
        if (document == null) {
            return null;
        }
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setRutaArchivo(document.getRutaArchivo());
        dto.setTipo(document.getTipo());
        dto.setEstado(document.getEstado());
        return dto;
    }
}