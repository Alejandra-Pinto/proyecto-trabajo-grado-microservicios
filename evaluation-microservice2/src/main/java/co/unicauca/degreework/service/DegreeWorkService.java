package co.unicauca.degreework.service;

import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.access.DocumentRepository;
import co.unicauca.degreework.domain.entities.DegreeWork;
import co.unicauca.degreework.domain.entities.Document;
import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.infra.dto.ActualizarEvaluacionDTO;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DegreeWorkService {

    private final DegreeWorkRepository repository;
    private final DocumentRepository documentRepository;
    private final DegreeWorkProducer degreeWorkProducer;

    public DegreeWorkService(DegreeWorkRepository repository,
                             DocumentRepository documentRepository,
                             DegreeWorkProducer degreeWorkProducer) {
        this.repository = repository;
        this.documentRepository = documentRepository;
        this.degreeWorkProducer = degreeWorkProducer;
    }

    /**
     * Obtener un trabajo de grado por ID
     */
    public DegreeWork obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * Actualizar estado y observaciones y ENVIAR DegreeWorkUpdateDTO a la cola
     */
    @Transactional
    public DegreeWork actualizarEstadoYObservaciones(ActualizarEvaluacionDTO dto) {

        if (dto == null || dto.getDegreeWorkId() == null) {
            throw new IllegalArgumentException("DTO inválido: se requiere degreeWorkId.");
        }

        DegreeWork degreeWork = repository.findById(dto.getDegreeWorkId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un DegreeWork con ID " + dto.getDegreeWorkId()
                ));

        // ---------------------------------------------------
        // 1️⃣ OBTENER EL ÚLTIMO DOCUMENTO
        // ---------------------------------------------------
        Document ultimoDoc = obtenerUltimoDocumento(degreeWork);

        if (ultimoDoc == null) {
            throw new IllegalStateException("No hay documentos asociados al trabajo.");
        }

        EnumEstadoDocument estadoAnterior = ultimoDoc.getEstado();
        String obsPrevias = degreeWork.getCorrecciones();

        // ---------------------------------------------------
        // 2️⃣ ACTUALIZAR ESTADO Y OBSERVACIONES
        // ---------------------------------------------------
        if (dto.getEstado() != null) {
            ultimoDoc.setEstado(dto.getEstado());
            ultimoDoc.setFechaActual(LocalDate.now());
        }

        if (dto.getObservaciones() != null) {
            degreeWork.setCorrecciones(dto.getObservaciones());
        }

        // ---------------------------------------------------
        // 3️⃣ GUARDAR
        // ---------------------------------------------------
        DegreeWork saved = repository.save(degreeWork);

        boolean cambioEstado = dto.getEstado() != null && !dto.getEstado().equals(estadoAnterior);
        boolean cambioObs = dto.getObservaciones() != null &&
                !dto.getObservaciones().equals(obsPrevias);

        // ---------------------------------------------------
        // 4️⃣ ENVIAR A COLA SOLO SI HAY CAMBIOS
        // ---------------------------------------------------
        if (cambioEstado || cambioObs) {
            enviarDegreeWorkUpdate(saved, dto.getEstado(), dto.getObservaciones());
        }

        return saved;
    }

    /**
     * Devuelve el último documento que tenga el DegreeWork (FormA, anteproyecto o carta)
     */
    private Document obtenerUltimoDocumento(DegreeWork degreeWork) {

        if (degreeWork.getCartasAceptacion() != null && !degreeWork.getCartasAceptacion().isEmpty()) {
            return degreeWork.getCartasAceptacion()
                    .get(degreeWork.getCartasAceptacion().size() - 1);
        }

        if (degreeWork.getAnteproyectos() != null && !degreeWork.getAnteproyectos().isEmpty()) {
            return degreeWork.getAnteproyectos()
                    .get(degreeWork.getAnteproyectos().size() - 1);
        }

        if (degreeWork.getFormatosA() != null && !degreeWork.getFormatosA().isEmpty()) {
            return degreeWork.getFormatosA()
                    .get(degreeWork.getFormatosA().size() - 1);
        }

        return null;
    }

    /**
     *  Enviar DegreeWorkUpdateDTO a RabbitMQ (este es el DTO correcto)
     */
    private void enviarDegreeWorkUpdate(DegreeWork degreeWork,
                                        EnumEstadoDocument nuevoEstado,
                                        String observaciones) {
        try {
            DegreeWorkUpdateDTO dto = DegreeWorkUpdateDTO.builder()
                    .degreeWorkId(degreeWork.getId().intValue())
                    .estado(nuevoEstado != null ? nuevoEstado.name() : null)
                    .correcciones(observaciones)
                    .build();

            degreeWorkProducer.sendUpdate(dto);

            System.out.println("📤 [RABBITMQ] DegreeWorkUpdateDTO enviado: " + dto);

        } catch (Exception e) {
            System.err.println("❌ Error enviando DegreeWorkUpdateDTO a la cola: " + e.getMessage());
        }
    }

    public DegreeWork asignarEvaluadores(Long degreeWorkId, List<User> emailsEvaluadores) {

        if (emailsEvaluadores.size() != 2) {
            throw new IllegalArgumentException("Debe asignar exactamente 2 evaluadores.");
        }

        DegreeWork degreeWork = repository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("Trabajo de grado no encontrado"));

        // Guardar evaluadores
        degreeWork.setEvaluadores(emailsEvaluadores);

        // Enviar evento a DegreeWork para actualizar estado
        DegreeWorkUpdateDTO event = DegreeWorkUpdateDTO.builder()
                .degreeWorkId(degreeWorkId.intValue())
                .estado("EN_REVISION")
                .correcciones("Evaluadores asignados por Jefe de Departamento")
                .build();

        degreeWorkProducer.sendUpdate(event);

        return repository.save(degreeWork);
    }

}
