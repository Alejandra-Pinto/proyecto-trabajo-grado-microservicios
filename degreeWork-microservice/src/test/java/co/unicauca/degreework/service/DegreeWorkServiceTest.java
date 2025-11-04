package co.unicauca.degreework.service;

import co.unicauca.degreework.access.DegreeWorkRepository;
import co.unicauca.degreework.access.DocumentRepository;
import co.unicauca.degreework.domain.entities.*;
import co.unicauca.degreework.domain.entities.builder.DegreeWorkBuilder;
import co.unicauca.degreework.domain.entities.builder.ProfessionalPracticeBuilder;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.domain.entities.enums.EnumEstadoDocument;
import co.unicauca.degreework.domain.entities.enums.EnumModalidad;
import co.unicauca.degreework.domain.entities.enums.EnumTipoDocumento;
import co.unicauca.degreework.infra.dto.DegreeWorkDTO;
import co.unicauca.degreework.infra.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.infra.dto.DocumentDTO;
import co.unicauca.degreework.infra.messaging.DegreeWorkProducer;
import co.unicauca.degreework.infra.messaging.NotificationProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DegreeWorkServiceTest {

    @Mock
    private DegreeWorkRepository degreeWorkRepository;

    @Mock
    private UserService userService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DegreeWorkProducer degreeWorkProducer;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private DegreeWorkService degreeWorkService;

    private User student;
    private User teacher;
    private User coDirector;
    private DegreeWorkDTO degreeWorkDTO;
    private DegreeWork existingDegreeWork;

    @BeforeEach
    void setUp() {
        // Setup users
        student = new User();
        student.setId(1L);
        student.setFirstName("Student");
        student.setLastName("One");
        student.setEmail("student1@unicauca.edu.co");
        student.setRole("STUDENT");

        teacher = new User();
        teacher.setId(2L);
        teacher.setFirstName("Teacher");
        teacher.setLastName("One");
        teacher.setEmail("teacher1@unicauca.edu.co");
        teacher.setRole("TEACHER");

        coDirector = new User();
        coDirector.setId(3L);
        coDirector.setFirstName("CoDirector");
        coDirector.setLastName("One");
        coDirector.setEmail("codirector1@unicauca.edu.co");
        coDirector.setRole("TEACHER");

        // Setup DTO
        degreeWorkDTO = new DegreeWorkDTO();
        degreeWorkDTO.setTitulo("Sistema de Gestión Académica");
        degreeWorkDTO.setDirectorEmail("teacher1@unicauca.edu.co");
        degreeWorkDTO.setEstudiantesEmails(List.of("student1@unicauca.edu.co"));
        degreeWorkDTO.setCodirectoresEmails(List.of("codirector1@unicauca.edu.co"));
        degreeWorkDTO.setObjetivoGeneral("Desarrollar un sistema integral de gestión académica");
        degreeWorkDTO.setObjetivosEspecificos(List.of("Analizar requisitos", "Diseñar arquitectura"));
        degreeWorkDTO.setFechaActual(LocalDate.now());
        degreeWorkDTO.setEstado(EnumEstadoDegreeWork.FORMATO_A);
        degreeWorkDTO.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);

        DocumentDTO formatoA = new DocumentDTO();
        formatoA.setRutaArchivo("/documents/formatoA.pdf");
        formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        degreeWorkDTO.setFormatosA(List.of(formatoA));

        // Setup existing degree work - COMPLETAR TODOS LOS CAMPOS NECESARIOS
        existingDegreeWork = new DegreeWork();
        existingDegreeWork.setId(1L);
        existingDegreeWork.setTitulo("Existing Work");
        existingDegreeWork.setDirectorProyecto(teacher);
        existingDegreeWork.setEstudiantes(List.of(student));
        existingDegreeWork.setCodirectoresProyecto(List.of(coDirector));
        existingDegreeWork.setEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
        existingDegreeWork.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL); // AÑADIR MODALIDAD
        existingDegreeWork.setFechaActual(LocalDate.now()); // AÑADIR FECHA
        existingDegreeWork.setObjetivoGeneral("Objetivo general existente");
        existingDegreeWork.setObjetivosEspecificos(List.of("Objetivo específico 1"));
    }

    @Test
    void whenRegistrarDegreeWorkWithValidData_thenSuccess() {
        // Given
        DegreeWorkBuilder builder = new ProfessionalPracticeBuilder();
        
        when(userService.obtenerPorEmail("teacher1@unicauca.edu.co")).thenReturn(teacher);
        when(userService.obtenerPorEmail("student1@unicauca.edu.co")).thenReturn(student);
        when(userService.obtenerPorEmail("codirector1@unicauca.edu.co")).thenReturn(coDirector);
        when(degreeWorkRepository.listByStudent("student1@unicauca.edu.co")).thenReturn(List.of());
        
        // Mock para que el save retorne un DegreeWork completo
        when(degreeWorkRepository.save(any(DegreeWork.class))).thenAnswer(invocation -> {
            DegreeWork saved = invocation.getArgument(0);
            saved.setId(1L); // Asignar ID
            saved.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL); // Asegurar modalidad
            return saved;
        });

        // When
        DegreeWork result = degreeWorkService.registrarDegreeWork(degreeWorkDTO, builder);

        // Then
        assertNotNull(result);
        verify(userService, times(3)).obtenerPorEmail(anyString());
        verify(degreeWorkRepository).save(any(DegreeWork.class));
        verify(degreeWorkProducer).sendDegreeWorkCreated(any());
        verify(notificationProducer).sendNotification(any());
    }

    @Test
    void whenRegistrarDegreeWorkWithNonExistingDirector_thenThrowsException() {
        // Given
        DegreeWorkBuilder builder = new ProfessionalPracticeBuilder();
        when(userService.obtenerPorEmail("teacher1@unicauca.edu.co")).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            degreeWorkService.registrarDegreeWork(degreeWorkDTO, builder);
        });
        assertTrue(exception.getMessage().contains("No se encontró el director"));
        verify(degreeWorkRepository, never()).save(any(DegreeWork.class));
    }

    @Test
    void whenRegistrarDegreeWorkWithStudentHavingActiveDocuments_thenThrowsException() {
        // Given
        DegreeWorkBuilder builder = new ProfessionalPracticeBuilder();
        
        // Mock student with active documents - crear un DegreeWork completo
        DegreeWork existingWorkWithActiveDocs = new DegreeWork();
        Document activeDocument = new Document();
        activeDocument.setEstado(EnumEstadoDocument.SEGUNDA_REVISION);
        activeDocument.setTipo(EnumTipoDocumento.FORMATO_A);
        existingWorkWithActiveDocs.setFormatosA(List.of(activeDocument));
        existingWorkWithActiveDocs.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);

        when(userService.obtenerPorEmail("teacher1@unicauca.edu.co")).thenReturn(teacher);
        when(userService.obtenerPorEmail("student1@unicauca.edu.co")).thenReturn(student);
        when(degreeWorkRepository.listByStudent("student1@unicauca.edu.co"))
                .thenReturn(List.of(existingWorkWithActiveDocs));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            degreeWorkService.registrarDegreeWork(degreeWorkDTO, builder);
        });
        assertTrue(exception.getMessage().contains("ya tiene un trabajo con documentos activos"));
        verify(degreeWorkRepository, never()).save(any(DegreeWork.class));
    }

    @Test
    void whenObtenerPorIdExisting_thenReturnsDegreeWork() {
        // Given
        when(degreeWorkRepository.findById(1L)).thenReturn(Optional.of(existingDegreeWork));

        // When
        DegreeWork result = degreeWorkService.obtenerPorId(1L);

        // Then
        assertNotNull(result);
        assertEquals("Existing Work", result.getTitulo());
        verify(degreeWorkRepository).findById(1L);
    }

    @Test
    void whenObtenerPorIdNonExisting_thenReturnsNull() {
        // Given
        when(degreeWorkRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        DegreeWork result = degreeWorkService.obtenerPorId(999L);

        // Then
        assertNull(result);
        verify(degreeWorkRepository).findById(999L);
    }

    @Test
    void whenListarTodos_thenReturnsAllDegreeWorks() {
        // Given
        List<DegreeWork> degreeWorks = List.of(existingDegreeWork);
        when(degreeWorkRepository.findAll()).thenReturn(degreeWorks);

        // When
        List<DegreeWork> result = degreeWorkService.listarTodos();

        // Then
        assertEquals(1, result.size());
        verify(degreeWorkRepository).findAll();
    }

    @Test
    void whenListarPorDocente_thenReturnsFilteredDegreeWorks() {
        // Given
        List<DegreeWork> teacherWorks = List.of(existingDegreeWork);
        when(degreeWorkRepository.listByTeacher("teacher1@unicauca.edu.co")).thenReturn(teacherWorks);

        // When
        List<DegreeWork> result = degreeWorkService.listarDegreeWorksPorDocente("teacher1@unicauca.edu.co");

        // Then
        assertEquals(1, result.size());
        verify(degreeWorkRepository).listByTeacher("teacher1@unicauca.edu.co");
    }

    @Test
    void whenListarPorEstudiante_thenReturnsFilteredDegreeWorks() {
        // Given
        List<DegreeWork> studentWorks = List.of(existingDegreeWork);
        when(degreeWorkRepository.listByStudent("student1@unicauca.edu.co")).thenReturn(studentWorks);

        // When
        List<DegreeWork> result = degreeWorkService.listarDegreeWorksPorEstudiante("student1@unicauca.edu.co");

        // Then
        assertEquals(1, result.size());
        verify(degreeWorkRepository).listByStudent("student1@unicauca.edu.co");
    }

    @Test
    void whenEliminarDegreeWork_thenCallsDelete() {
        // When
        degreeWorkService.eliminarDegreeWork(1L);

        // Then
        verify(degreeWorkRepository).deleteById(1L);
    }

    @Test
    void whenActualizarDesdeEvaluacionWithValidData_thenUpdatesDegreeWork() {
        // Given
        DegreeWorkUpdateDTO updateDTO = new DegreeWorkUpdateDTO();
        updateDTO.setDegreeWorkId(1);
        updateDTO.setEstado("ACEPTADO");
        updateDTO.setCorrecciones("Algunas correcciones menores");

        // Crear documento completo
        Document formatoA = new Document();
        formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        formatoA.setRutaArchivo("/documents/formatoA.pdf");
        formatoA.setFechaActual(LocalDate.now());
        
        existingDegreeWork.setFormatosA(List.of(formatoA));

        when(degreeWorkRepository.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(degreeWorkRepository.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);

        // When
        degreeWorkService.actualizarDesdeEvaluacion(updateDTO);

        // Then
        verify(degreeWorkRepository).findById(1L);
        verify(degreeWorkRepository).save(any(DegreeWork.class));
        assertEquals("Algunas correcciones menores", existingDegreeWork.getCorrecciones());
        assertEquals(EnumEstadoDocument.ACEPTADO, formatoA.getEstado());
    }

    @Test
    void whenActualizarDesdeEvaluacionWithInvalidDegreeWorkId_thenThrowsException() {
        // Given
        DegreeWorkUpdateDTO updateDTO = new DegreeWorkUpdateDTO();
        updateDTO.setDegreeWorkId(999);

        when(degreeWorkRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            degreeWorkService.actualizarDesdeEvaluacion(updateDTO);
        });
        assertTrue(exception.getMessage().contains("No se encontró el trabajo de grado"));
    }

    @Test
    void whenActualizarDesdeEvaluacionWithInvalidEstado_thenThrowsException() {
        // Given
        DegreeWorkUpdateDTO updateDTO = new DegreeWorkUpdateDTO();
        updateDTO.setDegreeWorkId(1);
        updateDTO.setEstado("INVALID_STATE");

        // Crear documento completo
        Document formatoA = new Document();
        formatoA.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        existingDegreeWork.setFormatosA(List.of(formatoA));

        when(degreeWorkRepository.findById(1L)).thenReturn(Optional.of(existingDegreeWork));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            degreeWorkService.actualizarDesdeEvaluacion(updateDTO);
        });
        assertTrue(exception.getMessage().contains("El estado recibido no es válido"));
    }

    // Test adicional para cuando no hay documentos
    @Test
    void whenActualizarDesdeEvaluacionWithNoDocuments_thenThrowsException() {
        // Given
        DegreeWorkUpdateDTO updateDTO = new DegreeWorkUpdateDTO();
        updateDTO.setDegreeWorkId(1);
        updateDTO.setEstado("ACEPTADO");

        // DegreeWork sin documentos
        existingDegreeWork.setFormatosA(null);
        existingDegreeWork.setAnteproyectos(null);
        existingDegreeWork.setCartasAceptacion(null);

        when(degreeWorkRepository.findById(1L)).thenReturn(Optional.of(existingDegreeWork));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            degreeWorkService.actualizarDesdeEvaluacion(updateDTO);
        });
        assertTrue(exception.getMessage().contains("No se encontró ningún documento"));
    }
}