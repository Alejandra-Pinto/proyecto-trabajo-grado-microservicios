// hexagonal/port/out/UserRepositoryPort.java
package co.unicauca.degreework.hexagonal.port.out.db;

import co.unicauca.degreework.hexagonal.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    User save(User user);

    // Métodos faltantes que necesitas
    List<User> findAll();
    Optional<User> findById(Long id);
    void deleteById(Long id);
    List<User> findByRole(String rol);
    List<User> findByProgram(String programa);
}