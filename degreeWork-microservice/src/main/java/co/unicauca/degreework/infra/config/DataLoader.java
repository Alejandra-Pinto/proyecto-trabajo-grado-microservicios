package co.unicauca.degreework.infra.config;

import co.unicauca.degreework.domain.entities.User;
import co.unicauca.degreework.access.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Clase que carga datos iniciales (profesores y estudiantes)
 * al iniciar la aplicación para propósitos de prueba.
 */
@Component
public class DataLoader {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
        List<User> usuarios = List.of(
                // ==== DOCENTES ====
                User.builder()
                        .id(1L)
                        .firstName("Carlos")
                        .lastName("Ramírez")
                        .email("carlosramirez@unicauca.edu.co")
                        .role("DOCENTE")
                        .program("Ingeniería de Sistemas")
                        .status("ACTIVO")
                        .build(),

                User.builder()
                        .id(2L)
                        .firstName("Laura")
                        .lastName("Gómez")
                        .email("lauragomez@unicauca.edu.co")
                        .role("DOCENTE")
                        .program("Ingeniería Electrónica")
                        .status("ACTIVO")
                        .build(),

                // ==== ESTUDIANTES ====
                User.builder()
                        .id(3L)
                        .firstName("Juan")
                        .lastName("Torres")
                        .email("juantorres@unicauca.edu.co")
                        .role("ESTUDIANTE")
                        .program("Ingeniería de Sistemas")
                        .status("ACTIVO")
                        .build(),

                User.builder()
                        .id(4L)
                        .firstName("Ana")
                        .lastName("López")
                        .email("analopez@unicauca.edu.co")
                        .role("ESTUDIANTE")
                        .program("Ingeniería de Sistemas")
                        .status("ACTIVO")
                        .build(),

                User.builder()
                        .id(5L)
                        .firstName("María")
                        .lastName("Córdoba")
                        .email("mariacordoba@unicauca.edu.co")
                        .role("ESTUDIANTE")
                        .program("Ingeniería Electrónica")
                        .status("ACTIVO")
                        .build()
        );

        userRepository.saveAll(usuarios);
        System.out.println("Usuarios de prueba cargados correctamente.");
        } else {
            System.out.println("Usuarios ya existen, no se insertaron nuevamente.");
        }
    }
}

