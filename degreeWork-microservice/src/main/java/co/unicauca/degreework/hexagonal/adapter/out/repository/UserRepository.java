package co.unicauca.degreework.hexagonal.adapter.out.repository;

import co.unicauca.degreework.hexagonal.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByProgram(String program);
}