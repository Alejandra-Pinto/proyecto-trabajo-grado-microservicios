package com.example.evaluation.infra.config;

import com.example.evaluation.entity.*;
import com.example.evaluation.entity.enums.*;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;
import com.example.evaluation.repository.HeadOfDepartmentRepository;

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
        private final HeadOfDepartmentRepository headOfDepartmentRepository;

        @Override
        public void run(String... args) {

                if (degreeWorkRepository.count() == 0 && evaluadorRepository.count() == 0) {

                        // 👨‍🏫 Crear y guardar evaluadores PRIMERO (evita duplicados)
                        Evaluador director = crearOObtenerEvaluador(
                                        "Dr. Carlos García", "Director", "carlos.garcia@unicauca.edu.co");

                        Evaluador codirector1 = crearOObtenerEvaluador(
                                        "MSc. Laura Torres", "Codirector", "laura.torres@unicauca.edu.co");

                        Evaluador codirector2 = crearOObtenerEvaluador(
                                        "Ing. Mateo Rojas", "Codirector", "mateo.rojas@unicauca.edu.co");

                        System.out.println("✅ Evaluadores guardados correctamente");

                        // 👔 Jefe de departamento
                        HeadOfDepartment jefeDepto = new HeadOfDepartment(
                                        "Luis", "Martínez", "3115550001", "Ingeniería de Sistemas",
                                        "luis.martinez@unicauca.edu.co", "12345");
                        headOfDepartmentRepository.save(jefeDepto);

                        // 👩‍🎓 Estudiantes (se crean directamente en el trabajo)
                        Student estudiante1 = new Student();
                        estudiante1.setId(1L);
                        estudiante1.setName("Dana Romero");
                        estudiante1.setEmail("dana.romero@unicauca.edu.co");

                        Student estudiante2 = new Student();
                        estudiante2.setId(2L);
                        estudiante2.setName("Juan Pérez");
                        estudiante2.setEmail("juan.perez@unicauca.edu.co");

                        // 📄 Documentos
                        Document formatoA = new Document();
                        formatoA.setId(1L);
                        formatoA.setUrl("https://repositorio.uni.edu/formatoA.pdf");
                        formatoA.setEstado(EnumEstadoDocument.ACEPTADO);

                        Document anteproyecto = new Document();
                        anteproyecto.setId(2L);
                        anteproyecto.setUrl("https://repositorio.uni.edu/anteproyecto.pdf");
                        anteproyecto.setEstado(EnumEstadoDocument.PRIMERA_REVISION);

                        Document carta = new Document();
                        carta.setId(3L);
                        carta.setUrl("https://repositorio.uni.edu/carta-aceptacion.pdf");
                        carta.setEstado(EnumEstadoDocument.ACEPTADO);

                        // 🎓 Trabajo de grado
                        DegreeWork trabajo = new DegreeWork();
                        trabajo.setTitulo("Plataforma Automatizada de Evaluación de Trabajos de Grado");
                        trabajo.setModalidad(EnumModalidad.INVESTIGACION);
                        trabajo.setFechaActual(LocalDate.now());
                        trabajo.setObjetivoGeneral(
                                        "Diseñar e implementar un sistema que automatice la evaluación de trabajos de grado.");
                        trabajo.setObjetivosEspecificos(List.of(
                                        "Analizar el proceso de evaluación actual.",
                                        "Desarrollar un módulo de carga y validación de documentos.",
                                        "Implementar notificaciones automáticas para los evaluadores."));
                        trabajo.setEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
                        trabajo.setDirectorProyecto(director); // Usa el evaluador ya guardado
                        trabajo.setCodirectoresProyecto(List.of(codirector1, codirector2)); // Usa evaluadores ya
                                                                                            // guardados
                        trabajo.setEstudiantes(List.of(estudiante1, estudiante2));
                        trabajo.setFormatosA(List.of(formatoA));
                        trabajo.setAnteproyectos(List.of(anteproyecto));
                        trabajo.setCartasAceptacion(List.of(carta));
                        trabajo.setCorrecciones(
                                        "Corregir referencias bibliográficas y mejorar la redacción del objetivo específico 2.");
                        trabajo.setNoAprobadoCount(0);

                        // 🔗 Enlazar documentos con el trabajo
                        formatoA.setDegreeWork(trabajo);
                        anteproyecto.setDegreeWork(trabajo);
                        carta.setDegreeWork(trabajo);

                        // 💾 Guardar trabajo
                        degreeWorkRepository.save(trabajo);

                        System.out.println("✅ Datos de prueba cargados correctamente (evaluadores, jefe y trabajo).");

                } else {
                        System.out.println("ℹ️ Ya existen registros en la base de datos, no se cargaron datos nuevos.");
                }
        }

        /**
         * Crea un evaluador si no existe, o lo obtiene si ya existe por correo
         */
        private Evaluador crearOObtenerEvaluador(String nombre, String rol, String correo) {
                return evaluadorRepository.findByCorreo(correo)
                                .orElseGet(() -> {
                                        Evaluador nuevoEvaluador = new Evaluador(nombre, rol, correo);
                                        return evaluadorRepository.save(nuevoEvaluador);
                                });
        }
}