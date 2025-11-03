package com.example.evaluation.service;

import com.example.evaluation.entity.Document;
import com.example.evaluation.entity.Evaluation;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.repository.DocumentRepository;
import com.example.evaluation.repository.EvaluationRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EvaluacionService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    /**
     * Crear una nueva evaluación
     */
    public Evaluation crearEvaluacion(Long documentId, Long evaluadorId, String resultado, String tipo) {
        // Buscar el documento y el evaluador
        Optional<Document> optionalDocument = documentRepository.findById(documentId);
        Optional<Evaluador> optionalEvaluador = evaluadorRepository.findById(evaluadorId);

        // Validar existencia
        if (optionalDocument.isEmpty()) {
            throw new RuntimeException("❌ Documento no encontrado con ID: " + documentId);
        }
        if (optionalEvaluador.isEmpty()) {
            throw new RuntimeException("❌ Evaluador no encontrado con ID: " + evaluadorId);
        }

        Document document = optionalDocument.get();
        Evaluador evaluador = optionalEvaluador.get();

        // Crear la nueva evaluación
        Evaluation evaluacion = new Evaluation();
        evaluacion.setDocument(document);
        evaluacion.setEvaluador(evaluador);
        evaluacion.setResultado(resultado);
        evaluacion.setType(tipo);
        evaluacion.setSentAt(LocalDateTime.now());

        // ✅ Guardar la evaluación
        Evaluation evaluacionGuardada = evaluationRepository.save(evaluacion);

        // (Opcional) Si quieres actualizar algo en el documento o degreeWork:
        // por ejemplo, cambiar su estado cuando se evalúa
        /*
         * if (document.getDegreeWork() != null) {
         * document.getDegreeWork().setEstadoP(EstadoDegreeWork.EN_EVALUACION);
         * documentRepository.save(document);
         * }
         */

        return evaluacionGuardada;
    }

    /**
     * Listar todas las evaluaciones
     */
    public List<Evaluation> listarEvaluaciones() {
        return evaluationRepository.findAll();
    }

    /**
     * Buscar una evaluación por su ID
     */
    public Evaluation obtenerPorId(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + id));
    }

    /**
     * Eliminar una evaluación por ID
     */
    public void eliminarEvaluacion(Long id) {
        if (!evaluationRepository.existsById(id)) {
            throw new RuntimeException("La evaluación no existe");
        }
        evaluationRepository.deleteById(id);
    }
}
