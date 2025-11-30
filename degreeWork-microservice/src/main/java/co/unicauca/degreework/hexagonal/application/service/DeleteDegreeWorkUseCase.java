package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteDegreeWorkUseCase {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    @Autowired
    public DeleteDegreeWorkUseCase(DegreeWorkRepositoryPort degreeWorkRepositoryPort) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
    }

    public void execute(Long id) {
        // Verificar que existe antes de eliminar
        DegreeWork degreeWork = degreeWorkRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el trabajo de grado con ID " + id));
        
        degreeWorkRepositoryPort.deleteById(id);
        System.out.println("Trabajo de grado con ID " + id + " eliminado correctamente.");
    }
}