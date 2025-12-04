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
        return formatosA.size(); // Ya no +1 porque el nuevo ya está agregado
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
        // FORMATO A
        // ============================
        if (dto.getFormatosA() != null && !dto.getFormatosA().isEmpty()) {
            DocumentDTO formatoADto = dto.getFormatosA().get(0);
            
            // Obtener el último Formato A para verificar estado y contador
            Document ultimoFormatoA = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
            
            // Verificar si podemos crear un nuevo Formato A
            if (ultimoFormatoA != null) {
                // Si hay un Formato A anterior, verificar su estado
                if (ultimoFormatoA.getEstado() == EnumEstadoDocument.ACEPTADO) {
                    System.out.println("⚠️ El Formato A ya está ACEPTADO. No se puede crear una nueva versión.");
                    return;
                }
                
                if (ultimoFormatoA.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    System.out.println("❌ El Formato A está RECHAZADO definitivamente. No se pueden crear más versiones.");
                    return;
                }
                
                System.out.println("📝 Creando nueva versión de Formato A. " +
                    "Estado anterior: " + ultimoFormatoA.getEstado() +
                    ", Contador actual: " + existente.getNoAprobadoCount());
            } else {
                System.out.println("📝 Creando PRIMER Formato A");
            }
            
            // Crear nuevo Formato A
            Document nuevoFormatoA = new Document();
            nuevoFormatoA.setRutaArchivo(formatoADto.getRutaArchivo());
            nuevoFormatoA.setFechaActual(LocalDate.now());
            nuevoFormatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            
            // Lógica CRÍTICA para mantener el estado y contador correcto
            if (ultimoFormatoA != null) {
                // Si hay un Formato A anterior, NO resetear el contador
                // Mantener el estado del último Formato A para la nueva versión
                
                // Determinar el estado inicial del nuevo documento
                if (formatoADto.getEstado() != null) {
                    // Si el DTO viene con estado explícito, usarlo
                    nuevoFormatoA.setEstado(formatoADto.getEstado());
                } else {
                    // Si no viene con estado, mantener el estado del último documento
                    nuevoFormatoA.setEstado(ultimoFormatoA.getEstado());
                }
                
                System.out.println("🔄 Manteniendo contador Formato A en: " + existente.getNoAprobadoCount());
            } else {
                // Es el primer Formato A
                if (formatoADto.getEstado() != null) {
                    nuevoFormatoA.setEstado(formatoADto.getEstado());
                } else {
                    nuevoFormatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
                }
                // Solo resetear contador si es el PRIMER Formato A
                existente.resetNoAprobadoCountFormatoA();
                System.out.println("🔄 Contador Formato A reseteado a 0 (primer documento)");
            }
            
            // Asegurar que la lista exista
            if (existente.getFormatosA() == null) {
                existente.setFormatosA(new ArrayList<>());
            }
            
            // Agregar el nuevo documento a la lista
            existente.getFormatosA().add(nuevoFormatoA);
            
            // Si el documento viene como NO_ACEPTADO o se marcó como tal, manejar revisión
            if (nuevoFormatoA.getEstado() == EnumEstadoDocument.NO_ACEPTADO) {
                existente.manejarRevision(nuevoFormatoA);
            }
            
            // DEBUG: Mostrar información completa
            System.out.println("✅ Formato A creado: " +
                "\n  - ID: " + nuevoFormatoA.getId() +
                "\n  - Estado: " + nuevoFormatoA.getEstado() +
                "\n  - Ruta: " + nuevoFormatoA.getRutaArchivo() +
                "\n  - Contador después: " + existente.getNoAprobadoCount() +
                "\n  - Total Formatos A: " + existente.getFormatosA().size() +
                "\n  - Último estado anterior: " + (ultimoFormatoA != null ? ultimoFormatoA.getEstado() : "N/A"));
        }

        // ============================
        // ANTEPROYECTO
        // ============================
        if (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()) {
            // Verificar que el Formato A esté aceptado
            Document ultimoFormatoA = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.FORMATO_A);
            if (ultimoFormatoA == null || ultimoFormatoA.getEstado() != EnumEstadoDocument.ACEPTADO) {
                throw new IllegalStateException("No se puede subir un anteproyecto hasta que el Formato A haya sido ACEPTADO.");
            }

            DocumentDTO anteDto = dto.getAnteproyectos().get(0);
            
            // Obtener el último anteproyecto
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
                
                System.out.println("📝 Creando nueva versión de Anteproyecto. " +
                    "Estado anterior: " + ultimoAnteproyecto.getEstado() +
                    ", Contador actual: " + existente.getNoAprobadoCountAnteproyecto());
            } else {
                System.out.println("📝 Creando PRIMER Anteproyecto");
            }
            
            // Crear nuevo anteproyecto
            Document nuevoAnteproyecto = new Document();
            nuevoAnteproyecto.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            nuevoAnteproyecto.setRutaArchivo(anteDto.getRutaArchivo());
            nuevoAnteproyecto.setFechaActual(LocalDate.now());
            
            // Lógica similar para anteproyectos
            if (ultimoAnteproyecto != null) {
                // Si hay anteproyecto anterior, mantener estado y NO resetear contador
                if (anteDto.getEstado() != null) {
                    nuevoAnteproyecto.setEstado(anteDto.getEstado());
                } else {
                    nuevoAnteproyecto.setEstado(ultimoAnteproyecto.getEstado());
                }
            } else {
                // Primer anteproyecto
                if (anteDto.getEstado() != null) {
                    nuevoAnteproyecto.setEstado(anteDto.getEstado());
                } else {
                    nuevoAnteproyecto.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
                }
                // Solo resetear contador si es el PRIMER anteproyecto
                existente.resetNoAprobadoCountAnteproyecto();
                System.out.println("🔄 Contador Anteproyecto reseteado a 0 (primer documento)");
            }
            
            // Asegurar que la lista exista
            if (existente.getAnteproyectos() == null) {
                existente.setAnteproyectos(new ArrayList<>());
            }
            
            // Agregar el nuevo documento a la lista
            existente.getAnteproyectos().add(nuevoAnteproyecto);
            
            // Si viene con estado NO_ACEPTADO, manejar revisión
            if (nuevoAnteproyecto.getEstado() == EnumEstadoDocument.NO_ACEPTADO) {
                existente.manejarRevision(nuevoAnteproyecto);
            }
            
            // Actualizar estado del DegreeWork a ANTEPROYECTO
            existente.setEstado(co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork.ANTEPROYECTO);
            
            System.out.println("✅ Anteproyecto creado: " +
                "\n  - Estado: " + nuevoAnteproyecto.getEstado() +
                "\n  - Contador: " + existente.getNoAprobadoCountAnteproyecto() +
                "\n  - Total Anteproyectos: " + existente.getAnteproyectos().size());
        }

        // ============================
        // CARTA DE ACEPTACIÓN
        // ============================
        if (dto.getCartasAceptacion() != null && !dto.getCartasAceptacion().isEmpty()) {
            DocumentDTO cartaDto = dto.getCartasAceptacion().get(0);
            
            // Verificar que el Anteproyecto esté aceptado
            Document ultimoAnteproyecto = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.ANTEPROYECTO);
            if (ultimoAnteproyecto == null || ultimoAnteproyecto.getEstado() != EnumEstadoDocument.ACEPTADO) {
                throw new IllegalStateException("No se puede subir una Carta de Aceptación hasta que el Anteproyecto haya sido ACEPTADO.");
            }
            
            Document ultimaCarta = existente.getUltimoDocumentoPorTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            
            if (ultimaCarta != null) {
                if (ultimaCarta.getEstado() == EnumEstadoDocument.ACEPTADO) {
                    System.out.println("⚠️ La Carta de Aceptación ya está ACEPTADA. No se puede crear una nueva versión.");
                    return;
                }
                
                if (ultimaCarta.getEstado() == EnumEstadoDocument.RECHAZADO) {
                    System.out.println("❌ La Carta de Aceptación está RECHAZADA definitivamente. No se pueden crear más versiones.");
                    return;
                }
                
                System.out.println("📝 Creando nueva versión de Carta de Aceptación. Estado anterior: " + ultimaCarta.getEstado());
            }
            
            // Crear nueva carta de aceptación
            Document nuevaCarta = new Document();
            nuevaCarta.setTipo(EnumTipoDocumento.CARTA_ACEPTACION);
            nuevaCarta.setRutaArchivo(cartaDto.getRutaArchivo());
            nuevaCarta.setFechaActual(LocalDate.now());
            
            if (ultimaCarta != null) {
                // Mantener estado de carta anterior
                if (cartaDto.getEstado() != null) {
                    nuevaCarta.setEstado(cartaDto.getEstado());
                } else {
                    nuevaCarta.setEstado(ultimaCarta.getEstado());
                }
            } else {
                // Primera carta
                if (cartaDto.getEstado() != null) {
                    nuevaCarta.setEstado(cartaDto.getEstado());
                } else {
                    nuevaCarta.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
                }
            }
            
            // Manejar revisión si viene como NO_ACEPTADO
            if (nuevaCarta.getEstado() == EnumEstadoDocument.NO_ACEPTADO) {
                existente.manejarRevision(nuevaCarta);
            }
            
            if (existente.getCartasAceptacion() == null) {
                existente.setCartasAceptacion(new ArrayList<>());
            }
            existente.getCartasAceptacion().add(nuevaCarta);
            
            System.out.println("✅ Carta de Aceptación creada: " +
                "\n  - Estado: " + nuevaCarta.getEstado() +
                "\n  - Total Cartas: " + existente.getCartasAceptacion().size());
        }
    }
    
}