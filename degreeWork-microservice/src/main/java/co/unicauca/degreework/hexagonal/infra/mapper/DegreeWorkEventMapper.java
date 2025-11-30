package co.unicauca.degreework.hexagonal.infra.mapper;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.application.dto.DocumentDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DegreeWorkEventMapper {

    public DegreeWorkCreatedEvent toCreatedEvent(DegreeWork degreeWork) {
        if (degreeWork == null) {
            return null;
        }

        DegreeWorkCreatedEvent event = new DegreeWorkCreatedEvent();
        event.setId(degreeWork.getId());
        
        // Convertir Value Objects a tipos primitivos
        event.setTitulo(degreeWork.getTitulo() != null ? degreeWork.getTitulo().getValor() : null);
        event.setFechaActual(degreeWork.getFechaActual() != null ? degreeWork.getFechaActual().getValor() : null);
        
        // Convertir participantes a emails
        event.setEstudiantesEmails(extractEmails(degreeWork.getEstudiantes()));
        event.setDirectorEmail(degreeWork.getDirectorProyecto() != null ? degreeWork.getDirectorProyecto().getEmail() : null);
        event.setCodirectoresEmails(extractEmails(degreeWork.getCodirectoresProyecto()));
        
        // Campos de enumeraciones como String
        event.setModalidad(degreeWork.getModalidad() != null ? degreeWork.getModalidad().name() : null);
        event.setEstado(degreeWork.getEstado() != null ? degreeWork.getEstado().name() : null);
        
        // Convertir documentos
        event.setFormatosA(toDocumentDTOs(degreeWork.getFormatosA()));
        event.setAnteproyectos(toDocumentDTOs(degreeWork.getAnteproyectos()));
        event.setCartasAceptacion(toDocumentDTOs(degreeWork.getCartasAceptacion()));
        
        return event;
    }

    // Reutilizar métodos auxiliares del mapper anterior o hacerlos públicos/estáticos
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