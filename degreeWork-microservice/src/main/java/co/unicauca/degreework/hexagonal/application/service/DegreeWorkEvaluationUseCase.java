package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class DegreeWorkEvaluationUseCase {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Autowired
    public DegreeWorkEvaluationUseCase(
            DegreeWorkRepositoryPort degreeWorkRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    public void actualizarDesdeEvaluacion(DegreeWorkUpdateDTO dto) {
        if (dto == null || dto.getDegreeWorkId() == null) {
            System.err.println("DTO inválido, ignorando...");
            return; // En lugar de lanzar IllegalArgumentException
        }

        System.out.println("📥 [RabbitMQ] Recibido mensaje de Evaluaciones: " + dto);

        Long id = dto.getDegreeWorkId().longValue();

        DegreeWork degreeWork = degreeWorkRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));

        // --- Obtener el último documento subido (de cualquier tipo) ---
        Document ultimoDoc = obtenerUltimoDocumento(degreeWork);

        if (ultimoDoc == null) {
            // Si no hay documento, solo actualizamos correcciones si vienen
            if (dto.getCorrecciones() != null && !dto.getCorrecciones().isBlank()) {
                degreeWork.setCorrecciones(dto.getCorrecciones());
                degreeWorkRepositoryPort.save(degreeWork);
                System.out.println("[Evaluaciones] No había documentos. Guardadas correcciones para DegreeWork ID " + id);
                return;
            }
            throw new IllegalStateException("No se encontró ningún documento asociado al trabajo de grado.");
        }

        // Guardar estado previo del documento para logs / conteo
        EnumEstadoDocument estadoPrevio = ultimoDoc.getEstado();
        boolean cambioEstado = false;

        // --- Actualizar el estado del último documento (si viene estado) ---
        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            try {
                EnumEstadoDocument nuevoEstado = EnumEstadoDocument.valueOf(dto.getEstado().toUpperCase());
                if (nuevoEstado != ultimoDoc.getEstado()) {
                    ultimoDoc.setEstado(nuevoEstado);
                    ultimoDoc.setFechaActual(LocalDate.now());
                    cambioEstado = true;
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado recibido no es válido: " + dto.getEstado(), e);
            }
        }

        // --- Actualizar las correcciones (observaciones) si vienen ---
        boolean cambioCorrecciones = false;
        if (dto.getCorrecciones() != null) {
            String prevCorrecciones = degreeWork.getCorrecciones() == null ? "" : degreeWork.getCorrecciones();
            if (!dto.getCorrecciones().equals(prevCorrecciones)) {
                degreeWork.setCorrecciones(dto.getCorrecciones());
                cambioCorrecciones = true;
            }
        }

        // Si hubo cambios en el documento o en correcciones, guardar
        if (cambioEstado || cambioCorrecciones) {
            degreeWorkRepositoryPort.save(degreeWork);
            System.out.println("[Evaluaciones] DegreeWork ID " + id + " actualizado. Estado doc: " +
                    estadoPrevio + " -> " + ultimoDoc.getEstado() + ", correcciones actualizadas: " + cambioCorrecciones);
        } else {
            System.out.println("[Evaluaciones] DegreeWork ID " + id + " recibido pero sin cambios (estado/correcciones).");
        }

        // --- Si el estado fue distinto a ACEPTADO, incrementar contador (si corresponde) ---
        if (cambioEstado && !"ACEPTADO".equalsIgnoreCase(ultimoDoc.getEstado().name())) {
            degreeWork.setNoAprobadoCount(degreeWork.getNoAprobadoCount() + 1);
            degreeWorkRepositoryPort.save(degreeWork);
            System.out.println("[Evaluaciones] Documento no aprobado. NoAprobadoCount incrementado a: " + degreeWork.getNoAprobadoCount());
        }
    }

    public void asignarEvaluadores(EvaluacionEventDTO dto) {
        if (dto == null || dto.getDegreeWorkId() == null) {
            throw new IllegalArgumentException("DTO de Evaluación inválido.");
        }

        DegreeWork degreeWork = degreeWorkRepositoryPort.findById(dto.getDegreeWorkId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trabajo de grado no encontrado con ID " + dto.getDegreeWorkId()
                ));

        // Convertir emails → Usuarios reales
        // USAR Collectors.toList() en lugar de .toList() para lista MUTABLE
        List<User> evaluadores = dto.getEvaluadores().stream()
                .map(email -> userRepositoryPort.findByEmail(email).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()); // ← CAMBIO AQUÍ

        if (evaluadores.size() != 2) {
            throw new IllegalStateException("Debe recibir exactamente 2 evaluadores válidos.");
        }

        degreeWork.setEvaluadores(evaluadores);
        degreeWorkRepositoryPort.save(degreeWork);

        System.out.println("✅ Evaluadores asignados para el trabajo " + dto.getDegreeWorkId());
    }

    public Document obtenerUltimoDocumento(DegreeWork degreeWork) {
        if (degreeWork.getCartasAceptacion() != null && !degreeWork.getCartasAceptacion().isEmpty()) {
            return degreeWork.getCartasAceptacion().get(degreeWork.getCartasAceptacion().size() - 1);
        } else if (degreeWork.getAnteproyectos() != null && !degreeWork.getAnteproyectos().isEmpty()) {
            return degreeWork.getAnteproyectos().get(degreeWork.getAnteproyectos().size() - 1);
        } else if (degreeWork.getFormatosA() != null && !degreeWork.getFormatosA().isEmpty()) {
            return degreeWork.getFormatosA().get(degreeWork.getFormatosA().size() - 1);
        }
        return null;
    }
}