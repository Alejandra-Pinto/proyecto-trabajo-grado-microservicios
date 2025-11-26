/*package co.unicauca.degreework.infra.config;

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

            // Evaluador
            User evaluador = User.builder()
                    .id(5L)
                    .firstName("Pedro")
                    .lastName("Gomez")
                    .email("pedro.gomez@unicauca.edu.co")
                    .role("PROFESSOR")
                    .program("Ingeniería de Sistemas")
                    .status("ACEPTADO")
                    .build();

            userRepository.saveAll(List.of(estudiante1, estudiante2, director, codirector, evaluador));

            // ============ DOCUMENTOS ============

            Document formatoA = new Document();
            formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA.setRutaArchivo("/docs/formatoA_aceptado.pdf");
            formatoA.setFechaActual(LocalDate.now().minusDays(10));
            formatoA.setEstado(EnumEstadoDocument.ACEPTADO);

            Document anteproyecto = new Document();
            anteproyecto.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            anteproyecto.setRutaArchivo("/docs/anteproyecto_v1.pdf");
            anteproyecto.setFechaActual(LocalDate.now());
            anteproyecto.setEstado(EnumEstadoDocument.PRIMERA_REVISION);

            // ============ TRABAJO DE GRADO 1 (En ANTEPROYECTO) ============

            DegreeWork degreeWork1 = DegreeWork.builder()
                    .titulo("Sistema de gestión de trabajos de grado")
                    .modalidad(EnumModalidad.INVESTIGACION)
                    .fechaActual(LocalDate.now())
                    .objetivoGeneral("Diseñar e implementar un sistema de gestión de trabajos de grado basado en microservicios.")
                    .objetivosEspecificos(List.of(
                            "Analizar los requerimientos funcionales y no funcionales.",
                            "Diseñar la arquitectura basada en microservicios.",
                            "Implementar los módulos principales."
                    ))
                    .estado(EnumEstadoDegreeWork.ANTEPROYECTO) // ✅ ESTADO VÁLIDO
                    .correcciones("")
                    .noAprobadoCount(0)
                    .build();

            // Asociar documentos
            degreeWork1.setFormatosA(new ArrayList<>(List.of(formatoA)));
            degreeWork1.setAnteproyectos(new ArrayList<>(List.of(anteproyecto)));

            // Asociar usuarios
            degreeWork1.setEstudiantes(new ArrayList<>(List.of(estudiante1, estudiante2)));
            degreeWork1.setDirectorProyecto(director);
            degreeWork1.setCodirectoresProyecto(new ArrayList<>(List.of(codirector)));

            // ============ TRABAJO DE GRADO 2 (En MONOGRAFIA con correcciones) ============

            DegreeWork degreeWork2 = DegreeWork.builder()
                    .titulo("Plataforma de aprendizaje adaptativo usando IA")
                    .modalidad(EnumModalidad.INVESTIGACION)
                    .fechaActual(LocalDate.now().minusDays(5))
                    .objetivoGeneral("Desarrollar una plataforma educativa que se adapte al ritmo de aprendizaje de cada estudiante utilizando técnicas de inteligencia artificial.")
                    .objetivosEspecificos(List.of(
                            "Recolectar datos de interacción de estudiantes",
                            "Implementar algoritmos de recomendación",
                            "Validar la efectividad del sistema"
                    ))
                    .estado(EnumEstadoDegreeWork.MONOGRAFIA) // ✅ ESTADO VÁLIDO (en lugar de CORRECCIONES)
                    .correcciones("Falta especificar la metodología de evaluación y los criterios de validación del sistema.")
                    .noAprobadoCount(1)
                    .build();

            // Documentos para el segundo trabajo
            Document formatoA2 = new Document();
            formatoA2.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA2.setRutaArchivo("/docs/formatoA2.pdf");
            formatoA2.setFechaActual(LocalDate.now().minusDays(15));
            formatoA2.setEstado(EnumEstadoDocument.ACEPTADO);

            Document anteproyecto2 = new Document();
            anteproyecto2.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            anteproyecto2.setRutaArchivo("/docs/anteproyecto2_v1.pdf");
            anteproyecto2.setFechaActual(LocalDate.now().minusDays(5));
            anteproyecto2.setEstado(EnumEstadoDocument.NO_ACEPTADO);

            degreeWork2.setFormatosA(new ArrayList<>(List.of(formatoA2)));
            degreeWork2.setAnteproyectos(new ArrayList<>(List.of(anteproyecto2)));
            degreeWork2.setEstudiantes(new ArrayList<>(List.of(estudiante1)));
            degreeWork2.setDirectorProyecto(director);

            // ============ TRABAJO DE GRADO 3 (En FORMATO_A) ============

            DegreeWork degreeWork3 = DegreeWork.builder()
                    .titulo("Análisis de seguridad en aplicaciones web modernas")
                    .modalidad(EnumModalidad.PRACTICA_PROFESIONAL)
                    .fechaActual(LocalDate.now().minusDays(20))
                    .objetivoGeneral("Identificar y analizar vulnerabilidades comunes en aplicaciones web desarrolladas con frameworks modernos.")
                    .objetivosEspecificos(List.of(
                            "Realizar pentesting en aplicaciones demo",
                            "Documentar vulnerabilidades encontradas",
                            "Proponer medidas de mitigación"
                    ))
                    .estado(EnumEstadoDegreeWork.FORMATO_A) // ✅ ESTADO VÁLIDO (en lugar de APROBADO)
                    .correcciones("")
                    .noAprobadoCount(0)
                    .build();

            Document formatoA3 = new Document();
            formatoA3.setTipo(EnumTipoDocumento.FORMATO_A);
            formatoA3.setRutaArchivo("/docs/formatoA3.pdf");
            formatoA3.setFechaActual(LocalDate.now().minusDays(25));
            formatoA3.setEstado(EnumEstadoDocument.ACEPTADO);

            Document anteproyecto3 = new Document();
            anteproyecto3.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            anteproyecto3.setRutaArchivo("/docs/anteproyecto3.pdf");
            anteproyecto3.setFechaActual(LocalDate.now().minusDays(20));
            anteproyecto3.setEstado(EnumEstadoDocument.ACEPTADO);

            degreeWork3.setFormatosA(new ArrayList<>(List.of(formatoA3)));
            degreeWork3.setAnteproyectos(new ArrayList<>(List.of(anteproyecto3)));
            degreeWork3.setEstudiantes(new ArrayList<>(List.of(estudiante2)));
            degreeWork3.setDirectorProyecto(codirector);

            // Guardar todos los trabajos
            degreeWorkRepository.saveAll(List.of(degreeWork1, degreeWork2, degreeWork3));

            System.out.println("✅ DataLoader: 3 trabajos de grado creados con estados válidos para pruebas de evaluación.");

        } else {
            System.out.println("ℹ️ DataLoader: Ya existen trabajos de grado, no se insertaron datos iniciales.");
        }
    }
}*/