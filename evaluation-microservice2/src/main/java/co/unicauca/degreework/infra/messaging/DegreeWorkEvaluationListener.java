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
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;

@Component
public class DegreeWorkEvaluationListener {
    
    private final DegreeWorkRepository degreeWorkRepository;
    private final EntityManager entityManager;

    public DegreeWorkEvaluationListener(DegreeWorkRepository degreeWorkRepository, EntityManager entityManager) {
        this.degreeWorkRepository = degreeWorkRepository;
        this.entityManager = entityManager;
    }

    @RabbitListener(queues = "degreework.queue")
    @Transactional
    public void onDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("📥 [DEGREEWORK LISTENER] Recibido nuevo trabajo de grado: " + event.getTitulo());
        System.out.println("📥 ID del evento: " + event.getId());
        
        try {
            // Verificar si existe
            boolean exists = degreeWorkRepository.existsById(event.getId());
            System.out.println("🔍 ¿Existe el ID " + event.getId() + " en la BD?: " + exists);
            
            if (exists) {
                System.out.println("⚠️ El DegreeWork con ID " + event.getId() + " ya existe en la BD");
                return;
            }

            // Convertir el Evento a Entidad DegreeWork
            DegreeWork degreeWork = convertEventToEntity(event);
            System.out.println("🔧 DegreeWork construido - ID: " + degreeWork.getId());
            
            // 🔥 SOLUCIÓN: Usar EntityManager para forzar PERSIST en lugar de MERGE
            entityManager.persist(degreeWork);
            entityManager.flush(); // Forzar el INSERT inmediatamente
            
            System.out.println("✅ [DEGREEWORK LISTENER] Trabajo de grado guardado ID: " + degreeWork.getId());
            
        } catch (Exception e) {
            System.err.println("❌ [DEGREEWORK LISTENER] Error guardando trabajo de grado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DegreeWork convertEventToEntity(DegreeWorkCreatedEvent event) {
        DegreeWork degreeWork = new DegreeWork();
        
        // Información básica
        degreeWork.setId(event.getId()); // ✅ Mantener el mismo ID
        degreeWork.setTitulo(event.getTitulo());
        degreeWork.setModalidad(EnumModalidad.valueOf(event.getModalidad()));
        degreeWork.setFechaActual(event.getFechaActual());
        degreeWork.setEstado(EnumEstadoDegreeWork.valueOf(event.getEstado()));
        degreeWork.setObjetivoGeneral("Objetivo general del trabajo");
        degreeWork.setObjetivosEspecificos(List.of("Objetivo 1", "Objetivo 2"));
         
        // Convertir documentos
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

    private Document convertDocumentDTOToEntity(DocumentDTO dto) {
        Document document = new Document();
        document.setTipo(EnumTipoDocumento.valueOf(dto.getTipo().name()));
        document.setRutaArchivo(dto.getRutaArchivo());
        document.setEstado(EnumEstadoDocument.valueOf(dto.getEstado().name()));
        return document;
    }
}
