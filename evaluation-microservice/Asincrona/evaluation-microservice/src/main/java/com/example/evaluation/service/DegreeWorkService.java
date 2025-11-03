package com.example.evaluation.service;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DegreeWorkService {

    @Autowired
    private DegreeWorkRepository degreeWorkRepository;

    @Autowired
    private EvaluadorRepository evaluadorRepository;

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

    // ✅ Asignar evaluadores por correo
    public DegreeWork asignarEvaluadoresPorCorreo(Integer degreeWorkId, String correoEvaluador1,
            String correoEvaluador2) {
        // Buscar el trabajo de grado
        DegreeWork degreeWork = degreeWorkRepository.findById(degreeWorkId)
                .orElseThrow(() -> new RuntimeException("❌ Trabajo de grado no encontrado"));

        // Buscar evaluadores por correo
        Evaluador evaluador1 = evaluadorRepository.findByCorreo(correoEvaluador1)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 1 no encontrado con correo: " + correoEvaluador1));
        Evaluador evaluador2 = evaluadorRepository.findByCorreo(correoEvaluador2)
                .orElseThrow(() -> new RuntimeException("❌ Evaluador 2 no encontrado con correo: " + correoEvaluador2));

        // Asignar evaluadores
        degreeWork.setEvaluador1(evaluador1);
        degreeWork.setEvaluador2(evaluador2);

        return degreeWorkRepository.save(degreeWork);
    }
}
