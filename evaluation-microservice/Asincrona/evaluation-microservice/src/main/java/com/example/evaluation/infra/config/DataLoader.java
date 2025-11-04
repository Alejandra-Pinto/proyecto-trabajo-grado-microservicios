package com.example.evaluation.infra.config;

import com.example.evaluation.entity.*;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.entity.enums.EnumEstadoDocument;
import com.example.evaluation.entity.enums.EnumModalidad;
import com.example.evaluation.entity.enums.EnumTipoDocumento;
import com.example.evaluation.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DegreeWorkRepository degreeWorkRepository;
    private final UserRepository userRepository;
    private final EvaluadorRepository evaluadorRepository; // para endpoints que buscan evaluadores por correo

    @Override
    public void run(String... args) {

        if (degreeWorkRepository.count() > 0) {
            System.out.println("ℹ️ Ya existen DegreeWorks. No se cargan datos nuevos.");
            return;
        }

        // ========= USERS (director, codirectores, estudiantes) =========
        User director = upsertUser(
                1L, "Carlos", "García", "carlos.garcia@unicauca.edu.co",
                "DIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

        User codirector1 = upsertUser(
                2L, "Laura", "Torres", "laura.torres@unicauca.edu.co",
                "CODIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

        User codirector2 = upsertUser(
                3L, "Mateo", "Rojas", "mateo.rojas@unicauca.edu.co",
                "CODIRECTOR", "Ingeniería de Sistemas", "ACTIVO");

        User estudiante1 = upsertUser(
                4L, "Dana", "Romero", "dana.romero@unicauca.edu.co",
                "ESTUDIANTE", "Ingeniería de Sistemas", "ACTIVO");

        User estudiante2 = upsertUser(
                5L, "Juan", "Pérez", "juan.perez@unicauca.edu.co",
                "ESTUDIANTE", "Ingeniería de Sistemas", "ACTIVO");

        System.out.println("✅ [DataLoader] Users sembrados/actualizados.");

        // ========= EVALUADORES (para búsquedas por correo en EvaluacionService)
        // =========
        upsertEvaluador("Dr. Carlos García", "Director", "carlos.garcia@unicauca.edu.co");
        upsertEvaluador("MSc. Laura Torres", "Codirector", "laura.torres@unicauca.edu.co");
        upsertEvaluador("Ing. Mateo Rojas", "Codirector", "mateo.rojas@unicauca.edu.co");
        System.out.println("✅ [DataLoader] Evaluadores sembrados/actualizados.");

        // ========= DOCUMENTOS =========
        Document formatoA = Document.builder()
                .id(null) // por si tu builder asigna algo, asegúrate de que quede null
                .tipo(EnumTipoDocumento.FORMATO_A)
                .estado(EnumEstadoDocument.ACEPTADO)
                .rutaArchivo("https://repo.uni/formatoA.pdf")
                .build();

        Document anteproyecto = Document.builder()
                .id(null)
                .tipo(EnumTipoDocumento.ANTEPROYECTO)
                .estado(EnumEstadoDocument.PRIMERA_REVISION)
                .rutaArchivo("https://repo.uni/anteproyecto.pdf")
                .build();

        Document carta = Document.builder()
                .id(null)
                .tipo(EnumTipoDocumento.CARTA_ACEPTACION)
                .estado(EnumEstadoDocument.ACEPTADO)
                .rutaArchivo("https://repo.uni/carta-aceptacion.pdf")
                .build();

        // ------ DegreeWork: agrega los docs directamente ------
        DegreeWork tg = DegreeWork.builder()
                .titulo("Plataforma Automatizada de Evaluación de Trabajos de Grado")
                .modalidad(EnumModalidad.INVESTIGACION)
                .fechaActual(LocalDate.now())
                .objetivoGeneral("Diseñar e implementar un sistema que automatice la evaluación…")
                .objetivosEspecificos(List.of(
                        "Analizar el proceso de evaluación actual.",
                        "Desarrollar un módulo de carga y validación de documentos.",
                        "Implementar notificaciones automáticas para los evaluadores."))
                .estado(EnumEstadoDegreeWork.ANTEPROYECTO)
                .directorProyecto(director) // User
                .codirectoresProyecto(List.of(codirector1, codirector2)) // List<User>
                .estudiantes(List.of(estudiante1, estudiante2)) // List<User>
                // Agrega los documentos sin haberlos persistido antes
                .formatosA(new ArrayList<>(List.of(formatoA)))
                .anteproyectos(new ArrayList<>(List.of(anteproyecto)))
                .cartasAceptacion(new ArrayList<>(List.of(carta)))
                .correcciones("Corregir referencias …")
                .noAprobadoCount(0)
                .build();

        degreeWorkRepository.save(tg); // <- esto persiste todo en cascada
        System.out.println("✅ Seed ok. DegreeWork ID=" + tg.getId());
    }

    // ================= helpers =================

    private User upsertUser(Long id, String first, String last, String email,
            String role, String program, String status) {
        return userRepository.findById(id).orElseGet(() -> userRepository.save(User.builder()
                .id(id)
                .firstName(first)
                .lastName(last)
                .email(email)
                .role(role)
                .program(program)
                .status(status)
                .build()));
    }

    private Evaluador upsertEvaluador(String nombre, String rol, String correo) {
        return evaluadorRepository.findByCorreo(correo)
                .orElseGet(() -> evaluadorRepository.save(new Evaluador(nombre, rol, correo)));
    }

}
