package co.unicauca.degreework.infra.config;

import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.enums.*;
import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.access.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final DegreeWorkRepository degreeWorkRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        if (degreeWorkRepository.count() == 0) {

            // ============ USUARIOS ============

            // Estudiantes
            User estudiante1 = User.builder()
                    .id(1L)
                    .firstName("Laura")
                    .lastName("Lopez")
                    .email("laura@unicauca.edu.co")
                    .role("STUDENT")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();

            User estudiante2 = User.builder()
                    .id(2L)
                    .firstName("Marcos")
                    .lastName("Jimenez")
                    .email("marcos@unicauca.edu.co")
                    .role("STUDENT")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();
        User estudiante3 = User.builder()
                    .id(5L)
                    .firstName("matias")
                    .lastName("gomez")
                    .email("matias@unicauca.edu.co")
                    .role("STUDENT")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();

            // Director
            User director = User.builder()
                    .id(3L)
                    .firstName("Carlos")
                    .lastName("Rodriguez")
                    .email("carlos.rodriguez@unicauca.edu.co")
                    .role("PROFESSOR")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();

            // Codirector
            User codirector = User.builder()
                    .id(4L)
                    .firstName("Ana")
                    .lastName("Ruiz")
                    .email("ana.ruiz@unicauca.edu.co")
                    .role("PROFESSOR")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();

            userRepository.saveAll(List.of(estudiante1, estudiante2, estudiante3, director, codirector));
            // ============ DOCUMENTOS ============

            Document formatoA = new Document();
            formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA.setRutaArchivo("/docs/formatoA_aceptado.pdf");
            formatoA.setFechaActual(LocalDate.now().minusDays(10));
            formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
/* 
            Document anteproyecto = new Document();
            anteproyecto.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            anteproyecto.setRutaArchivo("/docs/anteproyecto_v1.pdf");
            anteproyecto.setFechaActual(LocalDate.now());
            anteproyecto.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
 */
            // ============ TRABAJO DE GRADO ============

            DegreeWork degreeWork = DegreeWork.builder()
                    .titulo("Sistema de gestión de trabajos de grado")
                    .modalidad(EnumModalidad.INVESTIGACION)
                    .fechaActual(LocalDate.now())
                    .objetivoGeneral("Diseñar e implementar un sistema de gestión de trabajos de grado basado en microservicios.")
                    .objetivosEspecificos(List.of(
                            "Analizar los requerimientos funcionales y no funcionales.",
                            "Diseñar la arquitectura basada en microservicios.",
                            "Implementar los módulos principales."
                    ))
                    .estado(EnumEstadoDegreeWork.FORMATO_A)
                    .build();

            // Asociar documentos
            degreeWork.setFormatosA(new ArrayList<>(List.of(formatoA)));
            //degreeWork.setAnteproyectos(new ArrayList<>(List.of(anteproyecto)));

            // Asociar usuarios
            degreeWork.setEstudiantes(new ArrayList<>(List.of(estudiante1, estudiante2)));
            degreeWork.setDirectorProyecto(director);
            degreeWork.setCodirectoresProyecto(new ArrayList<>(List.of(codirector)));

            // Guardar en la base de datos
            degreeWorkRepository.save(degreeWork);

            System.out.println("✅ DataLoader: Trabajo de grado creado con usuarios, formato A y anteproyecto.");

        } else {
            System.out.println("ℹ️ DataLoader: Ya existen trabajos de grado, no se insertaron datos iniciales.");
        }
    }
}
