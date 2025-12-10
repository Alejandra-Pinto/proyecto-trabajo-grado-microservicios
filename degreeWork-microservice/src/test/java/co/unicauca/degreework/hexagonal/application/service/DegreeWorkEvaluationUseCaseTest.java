package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkUpdateDTO;
import co.unicauca.degreework.hexagonal.application.dto.EvaluacionEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DegreeWorkEvaluationUseCaseTest {

    @Mock
    private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private DegreeWorkEvaluationUseCase degreeWorkEvaluationUseCase;

    private DegreeWork degreeWork;
    private Document formatoA;
    private Document anteproyecto;
    private Document cartaAceptacion;

    @BeforeEach
    void setUp() {
        degreeWorkEvaluationUseCase = new DegreeWorkEvaluationUseCase(
                degreeWorkRepositoryPort,
                userRepositoryPort);

        // Configurar datos de prueba
        degreeWork = new DegreeWork();
        degreeWork.setId(1L);
        degreeWork.setCorrecciones("Correcciones iniciales");
        degreeWork.setNoAprobadoCount(0);

        formatoA = new Document();
        formatoA.setId(1L);
        formatoA.setTipo(co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento.FORMATO_A);
        formatoA.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        formatoA.setFechaActual(LocalDate.now());

        anteproyecto = new Document();
        anteproyecto.setId(2L);
        anteproyecto.setTipo(co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento.ANTEPROYECTO);
        anteproyecto.setEstado(EnumEstadoDocument.SEGUNDA_REVISION);
        anteproyecto.setFechaActual(LocalDate.now());

        cartaAceptacion = new Document();
        cartaAceptacion.setId(3L);
        cartaAceptacion.setTipo(co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento.CARTA_ACEPTACION);
        cartaAceptacion.setEstado(EnumEstadoDocument.ACEPTADO);
        cartaAceptacion.setFechaActual(LocalDate.now());
    }

    @Test
    void testActualizarDesdeEvaluacion_SuccessfulUpdateWithEstado() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("ACEPTADO");
        dto.setCorrecciones("Nuevas correcciones");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort).findById(1L);
        // Para ACEPTADO, solo se guarda una vez (no incrementa contador)
        verify(degreeWorkRepositoryPort, times(1)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_WithNoAprobadoState() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("NO_ACEPTADO");
        dto.setCorrecciones("Documento no cumple requisitos");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // Para NO_ACEPTADO, se guarda DOS veces:
        // 1. Para el cambio de estado
        // 2. Para incrementar el contador
        verify(degreeWorkRepositoryPort, times(2)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_OnlyCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setCorrecciones("Solo correcciones sin cambiar estado");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // Solo correcciones = se guarda una vez
        verify(degreeWorkRepositoryPort, times(1)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_NoChanges() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        // Sin estado ni correcciones

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));

        // Act - No debería lanzar excepción
        assertDoesNotThrow(() -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));

        // Assert - No se guarda nada
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_WithCartaAceptacionAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("RECHAZADO");
        dto.setCorrecciones("Correcciones");

        // Configurar múltiples documentos
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        degreeWork.setAnteproyectos(Collections.singletonList(anteproyecto));
        degreeWork.setCartasAceptacion(Collections.singletonList(cartaAceptacion));

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // RECHAZADO no es ACEPTADO, así que se guarda 2 veces
        verify(degreeWorkRepositoryPort, times(2)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_WithAnteproyectoAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("ACEPTADO");
        dto.setCorrecciones("Correcciones");

        // Configurar solo formato A y anteproyecto
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        degreeWork.setAnteproyectos(Collections.singletonList(anteproyecto));
        // Sin cartas de aceptación

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // ACEPTADO = se guarda solo una vez
        verify(degreeWorkRepositoryPort, times(1)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_WithFormatoAAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("SEGUNDA_REVISION");
        dto.setCorrecciones("Correcciones");

        // Configurar solo formato A
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        // Sin anteproyectos ni cartas de aceptación

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // SEGUNDA_REVISION no es ACEPTADO, así que se guarda 2 veces
        verify(degreeWorkRepositoryPort, times(2)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_NoDocuments_OnlyCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setCorrecciones("Correcciones sin documentos");

        // Sin documentos
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        // Solo correcciones sin documentos = se guarda una vez
        verify(degreeWorkRepositoryPort, times(1)).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_NoDocuments_NoCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        // Sin documentos y sin correcciones

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));

        assertEquals("No se encontró ningún documento asociado al trabajo de grado.", exception.getMessage());
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_InvalidEstado() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1L);
        dto.setEstado("ESTADO_INVALIDO");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));

        assertTrue(exception.getMessage().contains("El estado recibido no es válido: ESTADO_INVALIDO"));
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
    }

    @Test
    void testActualizarDesdeEvaluacion_NullDTO() {
        // Act
        // La implementación NO lanza excepción cuando el DTO es null, solo imprime
        // error y retorna
        assertDoesNotThrow(() -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(null));

        // Assert - No se debe llamar a ningún método del repositorio
        verify(degreeWorkRepositoryPort, never()).findById(any());
        verify(degreeWorkRepositoryPort, never()).save(any());
    }

    @Test
    void testActualizarDesdeEvaluacion_NullDegreeWorkId() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(null);

        // Act
        // La implementación NO lanza excepción cuando el ID es null, solo imprime error
        // y retorna
        assertDoesNotThrow(() -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));

        // Assert - No se debe llamar a ningún método del repositorio
        verify(degreeWorkRepositoryPort, never()).findById(any());
        verify(degreeWorkRepositoryPort, never()).save(any());
    }

    @Test
    void testActualizarDesdeEvaluacion_DegreeWorkNotFound() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(999L);

        when(degreeWorkRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));

        assertEquals("No se encontró el trabajo de grado con ID 999", exception.getMessage());
    }

    @Test
    void testAsignarEvaluadores_SuccessfulAssignment() {
        // Arrange
        EvaluacionEventDTO dto = new EvaluacionEventDTO();
        dto.setDegreeWorkId(1L);
        dto.setEvaluadores(Arrays.asList("evaluador1@unicauca.edu.co", "evaluador2@unicauca.edu.co"));

        User evaluador1 = new User();
        evaluador1.setEmail("evaluador1@unicauca.edu.co");
        User evaluador2 = new User();
        evaluador2.setEmail("evaluador2@unicauca.edu.co");

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(userRepositoryPort.findByEmail("evaluador1@unicauca.edu.co")).thenReturn(Optional.of(evaluador1));
        when(userRepositoryPort.findByEmail("evaluador2@unicauca.edu.co")).thenReturn(Optional.of(evaluador2));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.asignarEvaluadores(dto);

        // Assert
        verify(degreeWorkRepositoryPort).save(any(DegreeWork.class));
    }

    @Test
    void testAsignarEvaluadores_InvalidNumberOfEvaluators() {
        // Arrange
        EvaluacionEventDTO dto = new EvaluacionEventDTO();
        dto.setDegreeWorkId(1L);
        dto.setEvaluadores(Arrays.asList("evaluador1@unicauca.edu.co")); // Solo 1 evaluador

        User evaluador1 = new User();
        evaluador1.setEmail("evaluador1@unicauca.edu.co");

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(userRepositoryPort.findByEmail("evaluador1@unicauca.edu.co")).thenReturn(Optional.of(evaluador1));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> degreeWorkEvaluationUseCase.asignarEvaluadores(dto));

        assertEquals("Debe recibir exactamente 2 evaluadores válidos.", exception.getMessage());
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
    }

    @Test
    void testAsignarEvaluadores_WithInvalidEmails() {
        // Arrange
        EvaluacionEventDTO dto = new EvaluacionEventDTO();
        dto.setDegreeWorkId(1L);
        dto.setEvaluadores(Arrays.asList("evaluador1@unicauca.edu.co", "invalid@unicauca.edu.co"));

        User evaluador1 = new User();
        evaluador1.setEmail("evaluador1@unicauca.edu.co");

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(userRepositoryPort.findByEmail("evaluador1@unicauca.edu.co")).thenReturn(Optional.of(evaluador1));
        when(userRepositoryPort.findByEmail("invalid@unicauca.edu.co")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> degreeWorkEvaluationUseCase.asignarEvaluadores(dto));

        assertEquals("Debe recibir exactamente 2 evaluadores válidos.", exception.getMessage());
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
    }

    @Test
    void testAsignarEvaluadores_NullDTO() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> degreeWorkEvaluationUseCase.asignarEvaluadores(null));

        assertEquals("DTO de Evaluación inválido.", exception.getMessage());
    }

    @Test
    void testAsignarEvaluadores_NullDegreeWorkId() {
        // Arrange
        EvaluacionEventDTO dto = new EvaluacionEventDTO();
        dto.setDegreeWorkId(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> degreeWorkEvaluationUseCase.asignarEvaluadores(dto));

        assertEquals("DTO de Evaluación inválido.", exception.getMessage());
    }

    @Test
    void testAsignarEvaluadores_DegreeWorkNotFound() {
        // Arrange
        EvaluacionEventDTO dto = new EvaluacionEventDTO();
        dto.setDegreeWorkId(999L);
        dto.setEvaluadores(Arrays.asList("evaluador1@unicauca.edu.co", "evaluador2@unicauca.edu.co"));

        when(degreeWorkRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> degreeWorkEvaluationUseCase.asignarEvaluadores(dto));

        assertEquals("Trabajo de grado no encontrado con ID 999", exception.getMessage());
    }
}