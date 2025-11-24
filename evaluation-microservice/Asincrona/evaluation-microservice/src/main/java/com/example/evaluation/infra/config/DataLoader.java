package com.example.evaluation.infra.config;

import com.example.evaluation.entity.DegreeWork;
import com.example.evaluation.entity.Document;
import com.example.evaluation.entity.Evaluador;
import com.example.evaluation.entity.enums.EnumEstadoDegreeWork;
import com.example.evaluation.entity.enums.EnumEstadoDocument;
import com.example.evaluation.entity.enums.EnumModalidad;
import com.example.evaluation.entity.enums.EnumTipoDocumento;
import com.example.evaluation.repository.DegreeWorkRepository;
import com.example.evaluation.repository.EvaluadorRepository;

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
        private final EvaluadorRepository evaluadorRepository; // para endpoints que buscan evaluadores por correo

        @Override
        public void run(String... args) {

                if (degreeWorkRepository.count() > 0) {
                        System.out.println("ℹ️ Ya existen DegreeWorks. No se cargan datos nuevos.");
                        return;
                }

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
                                .id(1L)
                                .titulo("Plataforma Automatizada de Evaluación de Trabajos de Grado")
                                .modalidad(EnumModalidad.INVESTIGACION)
                                .fechaActual(LocalDate.now())
                                .objetivoGeneral("Diseñar e implementar un sistema que automatice la evaluación…")
                                .objetivosEspecificos(List.of(
                                                "Analizar el proceso de evaluación actual.",
                                                "Desarrollar un módulo de carga y validación de documentos.",
                                                "Implementar notificaciones automáticas para los evaluadores."))
                                .estado(EnumEstadoDegreeWork.ANTEPROYECTO)
                                .directorEmail("carlos.garcia@unicauca.edu.co")
                                .codirectoresEmails(
                                                List.of("laura.torres@unicauca.edu.co", "mateo.rojas@unicauca.edu.co"))
                                .estudiantesEmails(List.of("dana.romero@unicauca.edu.co", "juan.perez@unicauca.edu.co"))
                                // Agrega los documentos sin haberlos persistido antes
                                .formatosA(new ArrayList<>(List.of(formatoA)))
                                .anteproyectos(new ArrayList<>(List.of(anteproyecto)))
                                .cartasAceptacion(new ArrayList<>(List.of(carta)))
                                .correcciones("Corregir referencias …")
                                .noAprobadoCount(0)
                                .calificacion(null)
                                .build();

                degreeWorkRepository.save(tg); // <- esto persiste todo en cascada
                System.out.println("✅ Seed ok. DegreeWork ID=" + tg.getId());
        }

        // ================= helpers =================
        private Evaluador upsertEvaluador(String nombre, String rol, String correo) {
                return evaluadorRepository.findByCorreo(correo)
                                .orElseGet(() -> evaluadorRepository.save(new Evaluador(nombre, rol, correo)));
        }

}
