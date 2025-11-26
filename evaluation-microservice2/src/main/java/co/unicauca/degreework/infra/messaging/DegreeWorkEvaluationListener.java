package co.unicauca.degreework.infra.messaging;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.Document;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumTipoDocumento;
import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;
import co.unicauca.degreework.service.DegreeWorkService; 
import java.util.*;

@Component
public class DegreeWorkEvaluationListener {

    private final DegreeWorkRepository degreeWorkRepository;

    public DegreeWorkEvaluationListener(DegreeWorkRepository degreeWorkRepository) {
        this.degreeWorkRepository = degreeWorkRepository;
    }

    @RabbitListener(queues = "degreework.queue")
    public void onDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("📥 [DEGREEWORK LISTENER] Recibido nuevo trabajo de grado: " + event.getTitulo());
        
        try {
            // Convertir el Evento a Entidad DegreeWork
            DegreeWork degreeWork = convertEventToEntity(event);
            
            // Guardar en la base de datos
            degreeWorkRepository.save(degreeWork);
            
            System.out.println("✅ [DEGREEWORK LISTENER] Trabajo de grado guardado ID: " + degreeWork.getId());
            
        } catch (Exception e) {
            System.err.println("❌ [DEGREEWORK LISTENER] Error guardando trabajo de grado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convierte el Evento a Entidad DegreeWork
     */
    private DegreeWork convertEventToEntity(DegreeWorkCreatedEvent event) {
        DegreeWork degreeWork = new DegreeWork();
        
        // Información básica
        degreeWork.setId(event.getId());
        degreeWork.setTitulo(event.getTitulo());
        degreeWork.setModalidad(EnumModalidad.valueOf(event.getModalidad()));
        degreeWork.setFechaActual(event.getFechaActual());
        degreeWork.setEstado(EnumEstadoDegreeWork.valueOf(event.getEstado()));
        degreeWork.setObjetivoGeneral("Objetivo general del trabajo"); // Puedes ajustar esto
        degreeWork.setObjetivosEspecificos(List.of("Objetivo 1", "Objetivo 2")); // Ajustar según necesites
         
        // Convertir documentos DTO a entidades Document
        if (event.getFormatosA() != null) {
            degreeWork.setFormatosA(
                event.getFormatosA().stream()
                    .map(this::convertDocumentDTOToEntity)
                    .collect(Collectors.toList())
            );
        }
        
        if (event.getAnteproyectos() != null) {
            degreeWork.setAnteproyectos(
                event.getAnteproyectos().stream()
                    .map(this::convertDocumentDTOToEntity)
                    .collect(Collectors.toList())
            );
        }
        
        if (event.getCartasAceptacion() != null) {
            degreeWork.setCartasAceptacion(
                event.getCartasAceptacion().stream()
                    .map(this::convertDocumentDTOToEntity)
                    .collect(Collectors.toList())
            );
        }
        
        return degreeWork;
    }

    /**
     * Convierte DocumentDTO a entidad Document
     */
    private Document convertDocumentDTOToEntity(DocumentDTO dto) {
        Document document = new Document();
        document.setTipo(EnumTipoDocumento.valueOf(dto.getTipo().name()));
        document.setRutaArchivo(dto.getRutaArchivo());
        document.setEstado(EnumEstadoDocument.valueOf(dto.getEstado().name()));
        return document;
    }
}
