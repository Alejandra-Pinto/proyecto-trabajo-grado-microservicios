package co.unicauca.degreework.service;

import co.unicauca.degreework.access.UserRepository;
import co.unicauca.degreework.domain.entities.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar operaciones relacionadas con los usuarios
 * (estudiantes, docentes, codirectores, etc.)
 */
@Service
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene todos los usuarios del sistema
     */
    public List<User> listarUsuarios() {
        return repository.findAll();
    }

    /**
     * Obtiene un usuario por su ID
     */
    public User obtenerPorId(Long id) {
        Optional<User> userOpt = repository.findById(id);
        return userOpt.orElse(null);
    }

    /**
     * Obtiene un usuario por su email
     */
    public User obtenerPorEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    /**
     * Guarda o actualiza un usuario (normalmente no se usa directamente,
     * ya que los usuarios provienen del evento RabbitMQ)
     */
    public User guardarUsuario(User user) {
        return repository.save(user);
    }

    /**
     * Elimina un usuario por su ID
     */
    public void eliminarUsuario(Long id) {
        repository.deleteById(id);
    }

    /**
     * Filtra usuarios por rol (por ejemplo: "ESTUDIANTE", "DOCENTE")
     */
    public List<User> listarPorRol(String rol) {
        return repository.findByRole(rol);
    }

    /**
     * Filtra usuarios por programa académico
     */
    public List<User> listarPorPrograma(String programa) {
        return repository.findByProgram(programa);
    }
}
