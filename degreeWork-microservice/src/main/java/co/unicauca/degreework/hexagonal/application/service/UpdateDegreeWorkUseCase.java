package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.DocumentDTO;
import co.unicauca.degreework.hexagonal.application.dto.NotificationEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento;
import co.unicauca.degreework.hexagonal.domain.patterns.memento.DegreeWorkCaretaker;
import co.unicauca.degreework.hexagonal.domain.patterns.memento.DegreeWorkMemento;
import co.unicauca.degreework.hexagonal.domain.patterns.memento.DegreeWorkOriginator;
import co.unicauca.degreework.hexagonal.domain.service.DegreeWorkValidationService;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkEventMapper;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.messaging.EventPublisherPort;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UpdateDegreeWorkUseCase {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final DegreeWorkValidationService validationService;
    private final DegreeWorkEventMapper degreeWorkEventMapper;

    @Autowired
    public UpdateDegreeWorkUseCase(
            DegreeWorkRepositoryPort degreeWorkRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            EventPublisherPort eventPublisherPort,
            DegreeWorkValidationService validationService,
            DegreeWorkEventMapper degreeWorkEventMapper) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.validationService = validationService;
        this.degreeWorkEventMapper = degreeWorkEventMapper;
    }

    public DegreeWork execute(Long id, DegreeWorkDTO dto) {
        DegreeWork existente = degreeWorkRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));

        // Variables para controlar qué tipo de documento se está actualizando
        boolean isSubiendoFormatoA = false;
        boolean isSubiendoAnteproyecto = false;
        int numeroIntentoFormatoA = 0;
        
        // === INICIO MEMENTO ===
        DegreeWorkOriginator originator = new DegreeWorkOriginator(existente);
        DegreeWorkCaretaker caretaker = new DegreeWorkCaretaker();
        caretaker.addMemento(originator.save());
        System.out.println("[MEMENTO] Estado previo del trabajo de grado con ID " + id + " guardado correctamente.");
        // === FIN MEMENTO ===

        try {
            // --- Actualización de la información general ---
            // Actualizar Value Objects usando los métodos estáticos
            if (dto.getTitulo() != null) {
                existente.setTitulo(Titulo.createTitulo(dto.getTitulo()));
            }
            
            if (dto.getFechaActual() != null) {
                existente.setFechaActual(FechaCreacion.createFechaCreacion(dto.getFechaActual()));
            }
            
            // SOLO ACTUALIZAR SI NO ES NULL
            if (dto.getObjetivoGeneral() != null) {
                existente.setObjetivoGeneral(dto.getObjetivoGeneral());
            }
            
            if (dto.getCorrecciones() != null) {
                existente.setCorrecciones(dto.getCorrecciones());
            }
            
            if (dto.getEstado() != null) {
                existente.setEstado(dto.getEstado());
            }
            
            if (dto.getModalidad() != null) {
                existente.setModalidad(dto.getModalidad());
            }

            if (dto.getObjetivosEspecificos() != null) {
                existente.setObjetivosEspecificos(new ArrayList<>(dto.getObjetivosEspecificos()));
            }

            if (dto.getDirectorEmail() != null) {
                User nuevoDirector = userRepositoryPort.findByEmail(dto.getDirectorEmail())
                        .orElseThrow(() -> new IllegalArgumentException("No se encontró el director con correo: " + dto.getDirectorEmail()));
                existente.setDirectorProyecto(nuevoDirector);
            }

            if (dto.getEstudiantesEmails() != null && !dto.getEstudiantesEmails().isEmpty()) {
                List<User> estudiantes = validationService.validarYObternerEstudiantes(dto.getEstudiantesEmails(), userRepositoryPort);
                existente.setEstudiantes(estudiantes);
            }

            if (dto.getCodirectoresEmails() != null && !dto.getCodirectoresEmails().isEmpty()) {
                List<User> codirectores = validationService.validarYObternerCodirectores(dto.getCodirectoresEmails(), userRepositoryPort);
                existente.setCodirectoresProyecto(codirectores);
            }

            // --- Actualizar documentos ---
            actualizarDocumentos(dto, existente);

            // --- Determinar qué tipo de documento se está actualizando ---
            if (dto.getFormatosA() != null && !dto.getFormatosA().isEmpty()) {
                isSubiendoFormatoA = true;
                numeroIntentoFormatoA = calcularNumeroIntentoFormatoA(existente);
                System.out.println("[NOTIFICACIÓN] Se está subiendo Formato A - Intento #" + numeroIntentoFormatoA);
            }
            
            if (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()) {
                isSubiendoAnteproyecto = true;
                System.out.println("[NOTIFICACIÓN] Se está subiendo Anteproyecto");
            }

            // --- Guardar cambios ---
            DegreeWork saved = degreeWorkRepositoryPort.save(existente);

            // --- Enviar eventos ---
            // Evento para evaluation microservice usando el mapper
            DegreeWorkCreatedEvent event = degreeWorkEventMapper.toCreatedEvent(saved);
            eventPublisherPort.sendDegreeWorkCreated(event);
            
            // Enviar eventos de notificación según el tipo de documento actualizado
            if (isSubiendoFormatoA) {
                enviarEventoFormatoASubido(saved, numeroIntentoFormatoA);
            }
            
            if (isSubiendoAnteproyecto) {
                enviarEventoAnteproyectoSubido(saved);
            }

            System.out.println("[MEMENTO] Actualización completada correctamente.");
            return saved;

        } catch (Exception e) {
            // === REVERTIR SI FALLA ===
            if (caretaker.getHistorySize() > 0) {
                DegreeWorkMemento ultimoMemento = caretaker.getMemento(caretaker.getHistorySize() - 1);
                originator.restore(ultimoMemento);
                degreeWorkRepositoryPort.save(originator.getDegreeWork());
                System.out.println("[MEMENTO] Error detectado. Estado anterior restaurado para el trabajo de grado con ID " + id);
            } else {
                System.out.println("[MEMENTO] No había un estado previo guardado para restaurar.");
            }
            throw e;
        }
    }
    
    private int calcularNumeroIntentoFormatoA(DegreeWork degreeWork) {
        // Contar cuántos formatos A existen (incluyendo el nuevo)
        List<Document> formatosA = degreeWork.getFormatosA();
        if (formatosA == null) {
            return 1;
        }
        
        // Contar todos los formatos A, no solo los no rechazados
        // Porque cada intento cuenta, incluso si fue rechazado
        return formatosA.size() + 1; // +1 porque se está agregando uno nuevo
    }
    
    private void enviarEventoFormatoASubido(DegreeWork degreeWork, int numeroIntento) {
        NotificationEventDTO notificationEvent = new NotificationEventDTO();
        
        // Establecer valores básicos
        notificationEvent.setEventType("FORMATO_A_SUBIDO");
        notificationEvent.setTitle(degreeWork.getTitulo() != null ? degreeWork.getTitulo().getValor() : null);
        notificationEvent.setModality(degreeWork.getModalidad() != null ? degreeWork.getModalidad().name() : null);
        notificationEvent.setTimestamp(LocalDateTime.now());
        notificationEvent.setAttemptNumber(numeroIntento);
        
        // Notificar a TODOS los coordinadores
        notificationEvent.setTargetRole("COORDINATOR");
        
        // También al director del proyecto
        if (degreeWork.getDirectorProyecto() != null) {
            List<String> directorEmail = new ArrayList<>();
            directorEmail.add(degreeWork.getDirectorProyecto().getEmail());
            notificationEvent.setRecipientEmails(directorEmail);
            notificationEvent.setDirectorEmail(degreeWork.getDirectorProyecto().getEmail());
        }
        
        // Agregar codirectores si existen
        if (degreeWork.getCodirectoresProyecto() != null && !degreeWork.getCodirectoresProyecto().isEmpty()) {
            if (degreeWork.getCodirectoresProyecto().size() > 0) {
                notificationEvent.setCoDirector1Email(degreeWork.getCodirectoresProyecto().get(0).getEmail());
            }
            if (degreeWork.getCodirectoresProyecto().size() > 1) {
                notificationEvent.setCoDirector2Email(degreeWork.getCodirectoresProyecto().get(1).getEmail());
            }
        }
        
        System.out.println("[NOTIFICACIÓN] Enviando evento FORMATO_A_SUBIDO para trabajo: " + 
                         notificationEvent.getTitle() + " - Intento: " + numeroIntento);
        
        eventPublisherPort.sendNotification(notificationEvent);
    }
    
    private void enviarEventoAnteproyectoSubido(DegreeWork degreeWork) {
        NotificationEventDTO notificationEvent = new NotificationEventDTO();
        
        // Establecer valores básicos
        notificationEvent.setEventType("ANTEPROYECTO_SUBIDO");
        notificationEvent.setTitle(degreeWork.getTitulo() != null ? degreeWork.getTitulo().getValor() : null);
        notificationEvent.setModality(degreeWork.getModalidad() != null ? degreeWork.getModalidad().name() : null);
        notificationEvent.setTimestamp(LocalDateTime.now());
        
        // Notificar al jefe de departamento (DEPARTMENT_HEAD)
        notificationEvent.setTargetRole("DEPARTMENT_HEAD");
        
        // También al director del proyecto
        if (degreeWork.getDirectorProyecto() != null) {
            List<String> directorEmail = new ArrayList<>();
            directorEmail.add(degreeWork.getDirectorProyecto().getEmail());
            notificationEvent.setRecipientEmails(directorEmail);
            notificationEvent.setDirectorEmail(degreeWork.getDirectorProyecto().getEmail());
        }
        
        // Agregar codirectores si existen
        if (degreeWork.getCodirectoresProyecto() != null && !degreeWork.getCodirectoresProyecto().isEmpty()) {
            if (degreeWork.getCodirectoresProyecto().size() > 0) {
                notificationEvent.setCoDirector1Email(degreeWork.getCodirectoresProyecto().get(0).getEmail());
            }
            if (degreeWork.getCodirectoresProyecto().size() > 1) {
                notificationEvent.setCoDirector2Email(degreeWork.getCodirectoresProyecto().get(1).getEmail());
            }
        }
        
        System.out.println("[NOTIFICACIÓN] Enviando evento ANTEPROYECTO_SUBIDO para trabajo: " + 
                         notificationEvent.getTitle());
        
        eventPublisherPort.sendNotification(notificationEvent);
    }

    private void actualizarDocumentos(DegreeWorkDTO dto, DegreeWork existente) {
        // ============================
        // FORMATO A - SIEMPRE CREAR NUEVO DOCUMENTO PARA CADA REVISIÓN
        // ============================
        if (dto.getFormatosA() != null && !dto.getFormatosA().isEmpty()) {
            DocumentDTO formatoADto = dto.getFormatosA().get(0);
            
            // Obtener el último documento Formato A para verificar su estado
            Document ultimoFormatoA = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
            
            // Verificar si podemos crear un nuevo documento
            if (ultimoFormatoA != null) {
                // Verificar restricciones basadas en el estado del último documento
                if (ultimoFormatoA.getEstado() == EnumEstadoDocument.ACEPTADO) {
                    System.out.println("⚠️ El Formato A ya está ACEPTADO. No se puede crear una nueva versión.");
                    return; // No crear nuevo documento si ya está aceptado
                }
                
                if (ultimoFormatoA.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    System.out.println("❌ El Formato A está RECHAZADO definitivamente. No se pueden crear más versiones.");
                    return; // No crear nuevo documento si está rechazado definitivamente
                }
            }
            
            // SIEMPRE crear un nuevo documento Formato A (nueva versión)
            Document nuevoFormatoA = new Document();
            nuevoFormatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            nuevoFormatoA.setRutaArchivo(formatoADto.getRutaArchivo());
            nuevoFormatoA.setEstado(formatoADto.getEstado());
            nuevoFormatoA.setFechaActual(LocalDate.now());
            
            // Aplicar lógica de manejo de revisiones
            existente.manejarRevision(nuevoFormatoA);
            
            // Asegurar que la lista exista
            if (existente.getFormatosA() == null) {
                existente.setFormatosA(new ArrayList<>());
            }
            
            // Agregar el nuevo documento a la lista
            existente.getFormatosA().add(nuevoFormatoA);
            
            System.out.println("✅ Nuevo Formato A creado. Estado: " + nuevoFormatoA.getEstado() + 
                             ", Ruta: " + nuevoFormatoA.getRutaArchivo() +
                             ", Total Formatos A: " + existente.getFormatosA().size());
        }

        // ============================
        // ANTEPROYECTO
        // ============================
        if (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()) {
            // Verificar que el Formato A esté aceptado
            Document formatoA = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
            if (formatoA == null || formatoA.getEstado() != EnumEstadoDocument.ACEPTADO) {
                throw new IllegalStateException("No se puede subir un anteproyecto hasta que el Formato A haya sido ACEPTADO.");
            }

            DocumentDTO anteDto = dto.getAnteproyectos().get(0);
            
            // Obtener el último anteproyecto para verificar su estado
            Document ultimoAnteproyecto = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.ANTEPROYECTO);
            
            // Verificar si podemos crear un nuevo anteproyecto
            if (ultimoAnteproyecto != null) {
                if (ultimoAnteproyecto.getEstado() == EnumEstadoDocument.ACEPTADO) {
                    System.out.println("⚠️ El Anteproyecto ya está ACEPTADO. No se puede crear una nueva versión.");
                    return;
                }
                
                if (ultimoAnteproyecto.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    System.out.println("❌ El Anteproyecto está RECHAZADO definitivamente. No se pueden crear más versiones.");
                    return;
                }
            }
            
            // Crear nuevo anteproyecto
            Document nuevoAnteproyecto = new Document();
            nuevoAnteproyecto.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            nuevoAnteproyecto.setRutaArchivo(anteDto.getRutaArchivo());
            nuevoAnteproyecto.setEstado(anteDto.getEstado());
            nuevoAnteproyecto.setFechaActual(LocalDate.now());
            
            // Aplicar lógica de manejo de revisiones
            existente.manejarRevision(nuevoAnteproyecto);
            
            // Asegurar que la lista exista
            if (existente.getAnteproyectos() == null) {
                existente.setAnteproyectos(new ArrayList<>());
            }
            
            // Agregar el nuevo documento a la lista
            existente.getAnteproyectos().add(nuevoAnteproyecto);
            
            System.out.println("✅ Nuevo Anteproyecto creado. Estado: " + nuevoAnteproyecto.getEstado());
        }

        // ============================
        // CARTA DE ACEPTACIÓN
        // ============================
        if (dto.getCartasAceptacion() != null && !dto.getCartasAceptacion().isEmpty()) {
            DocumentDTO cartaDto = dto.getCartasAceptacion().get(0);
            
            // Obtener la última carta para verificar su estado
            Document ultimaCarta = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            
            // Verificar si podemos crear una nueva carta
            if (ultimaCarta != null) {
                if (ultimaCarta.getEstado() == EnumEstadoDocument.ACEPTADO) {
                    System.out.println("⚠️ La Carta de Aceptación ya está ACEPTADA. No se puede crear una nueva versión.");
                    return;
                }
                
                if (ultimaCarta.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    System.out.println("❌ La Carta de Aceptación está RECHAZADA definitivamente. No se pueden crear más versiones.");
                    return;
                }
            }
            
            // Crear nueva carta de aceptación
            Document nuevaCarta = new Document();
            nuevaCarta.setTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            nuevaCarta.setRutaArchivo(cartaDto.getRutaArchivo());
            nuevaCarta.setEstado(cartaDto.getEstado());
            nuevaCarta.setFechaActual(LocalDate.now());
            
            // Aplicar lógica de manejo de revisiones
            existente.manejarRevision(nuevaCarta);
            
            // Asegurar que la lista exista
            if (existente.getCartasAceptacion() == null) {
                existente.setCartasAceptacion(new ArrayList<>());
            }
            
            // Agregar el nuevo documento a la lista
            existente.getCartasAceptacion().add(nuevaCarta);
            
            System.out.println("✅ Nueva Carta de Aceptación creada. Estado: " + nuevaCarta.getEstado());
        }
    }
}