package com.example.evaluation.infra.config;

import com.example.evaluation.entity.*;
import com.example.evaluation.entity.enums.*;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import com.example.evaluation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DegreeWorkRepository degreeWorkRepository;
    private final EvaluadorRepository evaluadorRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        if (degreeWorkRepository.count() == 0) {

            // 👨‍🏫 Crear y guardar evaluadores
            Evaluador evaluador1 = crearOObtenerEvaluador("Dr. Carlos García", "Director", "carlos.garcia@unicauca.edu.co");
            Evaluador evaluador2 = crearOObtenerEvaluador("MSc. Laura Torres", "Codirector", "laura.torres@unicauca.edu.co");
            Evaluador evaluador3 = crearOObtenerEvaluador("Ing. Mateo Rojas", "Codirector", "mateo.rojas@unicauca.edu.co");

            System.out.println("✅ Evaluadores guardados correctamente");

            // 👩‍🎓 Crear usuarios (estudiantes, director y codirectores)
            User director = crearOObtenerUsuario(1L, "Carlos", "García", "carlos.garcia@unicauca.edu.co",
                    "DIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

            User codirector1 = crearOObtenerUsuario(2L, "Laura", "Torres", "laura.torres@unicauca.edu.co",
                    "CODIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

            User codirector2 = crearOObtenerUsuario(3L, "Mateo", "Rojas", "mateo.rojas@unicauca.edu.co",
                    "CODIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

            User estudiante1 = crearOObtenerUsuario(4L, "Dana", "Romero", "dana.romero@unicauca.edu.co",
                    "ESTUDIANTE", "Ingeniería de Sistemas", "ACTIVO");

            User estudiante2 = crearOObtenerUsuario(5L, "Juan", "Pérez", "juan.perez@unicauca.edu.co",
                    "ESTUDIANTE", "Ingeniería de Sistemas", "ACTIVO");

            // 📄 Documentos
            Document formatoA = Document.builder()
                    .rutaArchivo("https://repositorio.uni.edu/formatoA.pdf")
                    .estado(EnumEstadoDocument.ACEPTADO)
                    .build();

            Document anteproyecto = Document.builder()
                    .rutaArchivo("https://repositorio.uni.edu/anteproyecto.pdf")
                    .estado(EnumEstadoDocument.PRIMERA_REVISION)
                    .build();

            Document carta = Document.builder()
                    .rutaArchivo("https://repositorio.uni.edu/carta-aceptacion.pdf")
                    .estado(EnumEstadoDocument.ACEPTADO)
                    .build();

            // 🎓 Trabajo de grado
            DegreeWork trabajo = DegreeWork.builder()
                    .titulo("Plataforma Automatizada de Evaluación de Trabajos de Grado")
                    .modalidad(EnumModalidad.INVESTIGACION)
                    .fechaActual(LocalDate.now())
                    .objetivoGeneral("Diseñar e implementar un sistema que automatice la evaluación de trabajos de grado.")
                    .objetivosEspecificos(List.of(
                            "Analizar el proceso de evaluación actual.",
                            "Desarrollar un módulo de carga y validación de documentos.",
                            "Implementar notificaciones automáticas para los evaluadores."))
                    .estado(EnumEstadoDegreeWork.ANTEPROYECTO)
                    .directorProyecto(director)
                    .codirectoresProyecto(List.of(codirector1, codirector2))
                    .estudiantes(List.of(estudiante1, estudiante2))
                    .formatosA(List.of(formatoA))
                    .anteproyectos(List.of(anteproyecto))
                    .cartasAceptacion(List.of(carta))
                    .correcciones("Corregir referencias bibliográficas y mejorar la redacción del objetivo específico 2.")
                    .noAprobadoCount(0)
                    .build();

            // 💾 Guardar trabajo de grado completo
            degreeWorkRepository.save(trabajo);

            System.out.println("✅ Datos de prueba cargados correctamente (usuarios, evaluadores y trabajo).");

        } else {
            System.out.println("ℹ️ Ya existen registros en la base de datos, no se cargaron datos nuevos.");
        }
    }

    /**
     * Crea un evaluador si no existe, o lo obtiene si ya existe por correo.
     */
    private Evaluador crearOObtenerEvaluador(String nombre, String rol, String correo) {
        return evaluadorRepository.findByCorreo(correo)
                .orElseGet(() -> {
                    Evaluador nuevoEvaluador = new Evaluador(nombre, rol, correo);
                    return evaluadorRepository.save(nuevoEvaluador);
                });
    }

    /**
     * Crea un usuario si no existe, o lo obtiene si ya existe por correo.
     */
    private User crearOObtenerUsuario(Long id, String firstName, String lastName, String email,
                                      String role, String program, String status) {
        return userRepository.findById(id)
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(id)
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .role(role)
                        .program(program)
                        .status(status)
                        .build()));
    }
}
