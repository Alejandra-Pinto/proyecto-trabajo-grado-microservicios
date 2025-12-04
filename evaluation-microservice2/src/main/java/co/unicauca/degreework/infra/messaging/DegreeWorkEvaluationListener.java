package co.unicauca.degreework.infra.messaging;

import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.Document;
import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumTipoDocumento;
import co.unicauca.degreework.infra.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;
import co.unicauca.degreework.service.DegreeWorkService;
import co.unicauca.degreework.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;

@Component
public class DegreeWorkEvaluationListener {
    
    private final DegreeWorkRepository degreeWorkRepository;
    private final EntityManager entityManager;
    private final UserService userService;

    public DegreeWorkEvaluationListener(DegreeWorkRepository degreeWorkRepository, EntityManager entityManager, UserService userService) {
        this.degreeWorkRepository = degreeWorkRepository;
        this.entityManager = entityManager;
        this.userService = userService;
    }

    @RabbitListener(queues = "degreework.queue")
    @Transactional
    public void onDegreeWorkCreated(DegreeWorkCreatedEvent event) {
        System.out.println("📥 [DEGREEWORK LISTENER] Recibido evento para trabajo de grado: " + event.getTitulo());
        System.out.println("📥 ID del evento: " + event.getId());
        System.out.println("📥 Tipo de evento (implícito): " + (event.getEstado() != null ? event.getEstado() : "Actualización"));
        
        try {
            // Verificar si existe
            boolean exists = degreeWorkRepository.existsById(event.getId());
            System.out.println("🔍 ¿Existe el ID " + event.getId() + " en la BD?: " + exists);
            
            if (exists) {
                System.out.println("🔄 DegreeWork ya existe - Actualizando...");
                updateExistingDegreeWork(event);
            } else {
                System.out.println("🆕 DegreeWork no existe - Creando nuevo...");
                createNewDegreeWork(event);
            }
            
        } catch (Exception e) {
            System.err.println("❌ [DEGREEWORK LISTENER] Error procesando trabajo de grado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createNewDegreeWork(DegreeWorkCreatedEvent event) {
        // Convertir el Evento a Entidad DegreeWork
        DegreeWork degreeWork = convertEventToEntity(event);
        System.out.println("🔧 DegreeWork construido - ID: " + degreeWork.getId());
        
        // Usar EntityManager para forzar PERSIST
        entityManager.persist(degreeWork);
        entityManager.flush(); // Forzar el INSERT inmediatamente
        
        System.out.println("✅ [DEGREEWORK LISTENER] Trabajo de grado creado ID: " + degreeWork.getId());
    }
    
    private void updateExistingDegreeWork(DegreeWorkCreatedEvent event) {
        // Buscar el DegreeWork existente
        DegreeWork existingDegreeWork = degreeWorkRepository.findById(event.getId())
            .orElseThrow(() -> new RuntimeException("DegreeWork no encontrado para actualizar: " + event.getId()));
        
        System.out.println("🔄 Encontrado DegreeWork existente - Estado actual: " + existingDegreeWork.getEstado());
        
        // Actualizar campos básicos
        if (event.getTitulo() != null) {
            existingDegreeWork.setTitulo(event.getTitulo());
            System.out.println("📝 Actualizado título: " + event.getTitulo());
        }
        
        if (event.getModalidad() != null) {
            existingDegreeWork.setModalidad(EnumModalidad.valueOf(event.getModalidad()));
            System.out.println("📝 Actualizada modalidad: " + event.getModalidad());
        }
        
        if (event.getFechaActual() != null) {
            existingDegreeWork.setFechaActual(event.getFechaActual());
            System.out.println("📝 Actualizada fecha: " + event.getFechaActual());
        }
        
        if (event.getEstado() != null) {
            existingDegreeWork.setEstado(EnumEstadoDegreeWork.valueOf(event.getEstado()));
            System.out.println("📝 Actualizado estado: " + event.getEstado());
        }
        
        // 🔥 ACTUALIZAR DOCUMENTOS - ESTO ES LO MÁS IMPORTANTE
        updateDocuments(event, existingDegreeWork);
        
        // Guardar cambios
        degreeWorkRepository.save(existingDegreeWork);
        
        System.out.println("✅ [DEGREEWORK LISTENER] Trabajo de grado actualizado ID: " + existingDegreeWork.getId());
    }
    
    private void updateDocuments(DegreeWorkCreatedEvent event, DegreeWork degreeWork) {
        // Actualizar Formato A (si viene en el evento)
        if (event.getFormatosA() != null && !event.getFormatosA().isEmpty()) {
            System.out.println("📄 Actualizando Formato A...");
            
            // Obtener el último Formato A del evento
            DocumentDTO ultimoFormatoADto = event.getFormatosA().get(event.getFormatosA().size() - 1);
            
            // Buscar si ya existe un Formato A con el mismo tipo y estado
            Document formatoAExistente = findOrCreateDocument(
                degreeWork.getFormatosA(), 
                EnumTipoDocumento.FORMATO_A, 
                ultimoFormatoADto
            );
            
            // Si no existe, crear uno nuevo
            if (!degreeWork.getFormatosA().contains(formatoAExistente)) {
                degreeWork.getFormatosA().add(formatoAExistente);
                System.out.println("📄 Nuevo Formato A agregado - Estado: " + formatoAExistente.getEstado());
            } else {
                System.out.println("📄 Formato A actualizado - Estado: " + formatoAExistente.getEstado());
            }
        }
        
        // Actualizar Anteproyecto (si viene en el evento)
        if (event.getAnteproyectos() != null && !event.getAnteproyectos().isEmpty()) {
            System.out.println("📄 Actualizando Anteproyecto...");
            
            DocumentDTO ultimoAnteproyectoDto = event.getAnteproyectos().get(event.getAnteproyectos().size() - 1);
            
            Document anteproyectoExistente = findOrCreateDocument(
                degreeWork.getAnteproyectos(), 
                EnumTipoDocumento.ANTEPROYECTO, 
                ultimoAnteproyectoDto
            );
            
            if (!degreeWork.getAnteproyectos().contains(anteproyectoExistente)) {
                degreeWork.getAnteproyectos().add(anteproyectoExistente);
                System.out.println("📄 Nuevo Anteproyecto agregado - Estado: " + anteproyectoExistente.getEstado());
            } else {
                System.out.println("📄 Anteproyecto actualizado - Estado: " + anteproyectoExistente.getEstado());
            }
        }
        
        // Actualizar Cartas de Aceptación (si viene en el evento)
        if (event.getCartasAceptacion() != null && !event.getCartasAceptacion().isEmpty()) {
            System.out.println("📄 Actualizando Carta de Aceptación...");
            
            DocumentDTO ultimaCartaDto = event.getCartasAceptacion().get(event.getCartasAceptacion().size() - 1);
            
            Document cartaExistente = findOrCreateDocument(
                degreeWork.getCartasAceptacion(), 
                EnumTipoDocumento.CARTA_ACEPTACION, 
                ultimaCartaDto
            );
            
            if (!degreeWork.getCartasAceptacion().contains(cartaExistente)) {
                degreeWork.getCartasAceptacion().add(cartaExistente);
                System.out.println("📄 Nueva Carta de Aceptación agregada - Estado: " + cartaExistente.getEstado());
            } else {
                System.out.println("📄 Carta de Aceptación actualizada - Estado: " + cartaExistente.getEstado());
            }
        }
    }
    
    private Document findOrCreateDocument(List<Document> existingDocuments, 
                                         EnumTipoDocumento tipo, 
                                         DocumentDTO dto) {
        // Buscar documento existente con el mismo tipo
        if (existingDocuments != null && !existingDocuments.isEmpty()) {
            for (Document doc : existingDocuments) {
                if (doc.getTipo() == tipo) {
                    // Actualizar el documento existente
                    doc.setRutaArchivo(dto.getRutaArchivo());
                    doc.setEstado(EnumEstadoDocument.valueOf(dto.getEstado().name()));
                    return doc;
                }
            }
        }
        
        // Si no existe, crear uno nuevo
        return convertDocumentDTOToEntity(dto);
    }

    private DegreeWork convertEventToEntity(DegreeWorkCreatedEvent event) {
        DegreeWork degreeWork = new DegreeWork();
        
        // Información básica
        degreeWork.setId(event.getId());
        degreeWork.setTitulo(event.getTitulo());
        
        if (event.getModalidad() != null) {
            degreeWork.setModalidad(EnumModalidad.valueOf(event.getModalidad()));
        }
        
        if (event.getDirectorEmail() != null && !event.getDirectorEmail().trim().isEmpty()) {
            User director = userService.obtenerPorEmail(event.getDirectorEmail());
            if (director != null) {
                degreeWork.setDirectorProyecto(director);
                System.out.println("Director asignado: " + director.getEmail());
            } else {
                System.out.println("No se encontró usuario con email: " + event.getDirectorEmail());
            }
        }
        
        // Estudiantes
        if (event.getEstudiantesEmails() != null && !event.getEstudiantesEmails().isEmpty()) {
            List<User> estudiantes = new ArrayList<>();
            for (String email : event.getEstudiantesEmails()) {
                if (email != null && !email.trim().isEmpty()) {
                    User estudiante = userService.obtenerPorEmail(email.trim());
                    if (estudiante != null) {
                        estudiantes.add(estudiante);
                        System.out.println("Estudiante asignado: " + estudiante.getEmail());
                    } else {
                        System.out.println("No se encontró estudiante con email: " + email);
                    }
                }
            }
            degreeWork.setEstudiantes(estudiantes);
        }
        
        // Codirectores
        if (event.getCodirectoresEmails() != null && !event.getCodirectoresEmails().isEmpty()) {
            List<User> codirectores = new ArrayList<>();
            for (String email : event.getCodirectoresEmails()) {
                if (email != null && !email.trim().isEmpty()) {
                    User codirector = userService.obtenerPorEmail(email.trim());
                    if (codirector != null) {
                        codirectores.add(codirector);
                        System.out.println("✅ Codirector asignado: " + codirector.getEmail());
                    } else {
                        System.out.println("⚠️ No se encontró codirector con email: " + email);
                    }
                }
            }
            degreeWork.setCodirectoresProyecto(codirectores);
        }
        
        degreeWork.setFechaActual(event.getFechaActual());
        
        if (event.getEstado() != null) {
            degreeWork.setEstado(EnumEstadoDegreeWork.valueOf(event.getEstado()));
        }
        
        // Usar valores por defecto si no están en el evento
        if (degreeWork.getObjetivoGeneral() == null) {
            degreeWork.setObjetivoGeneral("Objetivo general del trabajo");
        }
        
        if (degreeWork.getObjetivosEspecificos() == null || degreeWork.getObjetivosEspecificos().isEmpty()) {
            degreeWork.setObjetivosEspecificos(List.of("Objetivo 1", "Objetivo 2"));
        }
        
        // Inicializar listas de documentos
        degreeWork.setFormatosA(new ArrayList<>());
        degreeWork.setAnteproyectos(new ArrayList<>());
        degreeWork.setCartasAceptacion(new ArrayList<>());
        
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
        
        // Inicializar evaluadores (lista vacía por defecto)
        degreeWork.setEvaluadores(new ArrayList<>());
        
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