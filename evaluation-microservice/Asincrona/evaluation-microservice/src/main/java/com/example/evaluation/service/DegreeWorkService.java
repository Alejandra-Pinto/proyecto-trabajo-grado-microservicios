package com.example.evaluation.service;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.infra.dto.DegreeWorkAssignmentDTO;
import com.example.evaluation.infra.messaging.DegreeWorkPublisher;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DegreeWorkService {

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

    @Autowired
    private DegreeWorkPublisher degreeWorkPublisher;

    // ✅ Listar los anteproyectos
    public List<DegreeWork> listarAnteproyectos() {
        return degreeWorkRepository.findByEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
    }

    // ✅ Listar todos los trabajos de grado
    public List<DegreeWork> listarTodos() {
        return degreeWorkRepository.findAll();
    }

    // ✅ Obtener trabajo de grado por correo del estudiante
    public DegreeWork obtenerPorCorreo(String correo) {
        return degreeWorkRepository.findByCorreoEstudiante(correo)
                .orElseThrow(() -> new RuntimeException(
                        "❌ No se encontró trabajo de grado del estudiante con correo: " + correo));
    }

    // ✅ Asignar evaluadores por correo y publicar en RabbitMQ
    @Transactional
    public DegreeWork asignarEvaluadoresPorCorreo(Integer degreeWorkId, String correoEvaluador1,
            String correoEvaluador2) {
        // 1️⃣ Buscar el trabajo de grado
        DegreeWork degreeWork = degreeWorkRepository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("❌ Trabajo de grado no encontrado con ID: " + degreeWorkId));

        // 2️⃣ Buscar evaluadores por correo
        Evaluador evaluador1 = evaluadorRepository.findByCorreo(correoEvaluador1)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 1 no encontrado con correo: " + correoEvaluador1));

        Evaluador evaluador2 = evaluadorRepository.findByCorreo(correoEvaluador2)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 2 no encontrado con correo: " + correoEvaluador2));

        // 3️⃣ Asignar evaluadores
        degreeWork.setEvaluador1(evaluador1);
        degreeWork.setEvaluador2(evaluador2);

        // 4️⃣ Guardar en base de datos
        DegreeWork trabajoActualizado = degreeWorkRepository.save(degreeWork);

        // 5️⃣ Crear el DTO para RabbitMQ
        DegreeWorkAssignmentDTO assignmentDTO = new DegreeWorkAssignmentDTO(
                trabajoActualizado.getId(),
                trabajoActualizado.getTitulo(),
                evaluador1.getCorreo(),
                evaluador1.getNombre(),
                evaluador2.getCorreo(),
                evaluador2.getNombre(),
                trabajoActualizado.getEstado().name());

        // 6️⃣ Publicar en la cola degreework.queue
        degreeWorkPublisher.publicarAsignacionEvaluadores(assignmentDTO);

        System.out.println("✅ Evaluadores asignados y notificación enviada a DEGREEWORK SERVICE");

        return trabajoActualizado;
    }
}