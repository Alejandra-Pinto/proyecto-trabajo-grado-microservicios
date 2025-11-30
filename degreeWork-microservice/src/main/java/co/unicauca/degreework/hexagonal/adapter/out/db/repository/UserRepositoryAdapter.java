// hexagonal/adapter/out/db/repository/UserRepositoryAdapter.java
package co.unicauca.degreework.hexagonal.adapter.out.db.repository;

import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import co.unicauca.degreework.hexagonal.adapter.out.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository; // Tu JPA repository

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findByRole(String rol) {
        return userRepository.findByRole(rol);
    }

    @Override
    public List<User> findByProgram(String programa) {
        return userRepository.findByProgram(programa);
    }
}