package co.unicauca.degreework.hexagonal.domain.service;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DegreeWorkValidationService {

    private final DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    public DegreeWorkValidationService(DegreeWorkRepositoryPort degreeWorkRepositoryPort) {
        this.degreeWorkRepositoryPort = degreeWorkRepositoryPort;
    }

    public List<User> validarYObternerEstudiantes(List<String> estudiantesEmails, UserRepositoryPort userRepositoryPort) {
        List<User> estudiantes = new ArrayList<>();
        for (String email : estudiantesEmails) {
            User estudiante = userRepositoryPort.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el estudiante con correo: " + email));
            estudiantes.add(estudiante);
        }
        validarEstudianteSinTrabajosActivos(estudiantes);
        return estudiantes;
    }

    public List<User> validarYObternerCodirectores(List<String> codirectoresEmails, UserRepositoryPort userRepositoryPort) {
        if (codirectoresEmails == null) return new ArrayList<>();
        
        List<User> codirectores = new ArrayList<>();
        for (String email : codirectoresEmails) {
            User codirector = userRepositoryPort.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el codirector con correo: " + email));
            codirectores.add(codirector);
        }
        return codirectores;
    }

    public void validarEstudianteSinTrabajosActivos(List<User> estudiantes) {
        for (User estudiante : estudiantes) {
            List<DegreeWork> trabajos = degreeWorkRepositoryPort.listByStudent(estudiante.getEmail());

            boolean tieneDocumentoActivo = trabajos.stream().anyMatch(this::tieneDocumentosNoRechazados);

            if (tieneDocumentoActivo) {
                throw new IllegalStateException(
                    "El estudiante con correo " + estudiante.getEmail() +
                    " ya tiene un trabajo con documentos activos (no rechazados) y no puede registrar otro."
                );
            }
        }
    }

    public boolean tieneDocumentosNoRechazados(DegreeWork degreeWork) {
        List<Document> todosDocs = new ArrayList<>();
        if (degreeWork.getFormatosA() != null) todosDocs.addAll(degreeWork.getFormatosA());
        if (degreeWork.getAnteproyectos() != null) todosDocs.addAll(degreeWork.getAnteproyectos());
        if (degreeWork.getCartasAceptacion() != null) todosDocs.addAll(degreeWork.getCartasAceptacion());

        if (!todosDocs.isEmpty()) {
            Document ultimo = todosDocs.get(todosDocs.size() - 1);
            return ultimo.getEstado() != EnumEstadoDocument.RECHAZADO;
        }
        return false;
    }
}