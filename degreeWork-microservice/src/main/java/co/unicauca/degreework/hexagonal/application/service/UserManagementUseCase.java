package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserManagementUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Autowired
    public UserManagementUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepositoryPort.findByEmail(email);
    }

    public User save(User user) {
        return userRepositoryPort.save(user);
    }

    public void deleteById(Long id) {
        userRepositoryPort.deleteById(id);
    }

    public List<User> findByRole(String rol) {
        return userRepositoryPort.findByRole(rol);
    }

    public List<User> findByProgram(String programa) {
        return userRepositoryPort.findByProgram(programa);
    }
}