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
                userRepositoryPort
        );

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
        dto.setDegreeWorkId(1);
        dto.setEstado("ACEPTADO");
        dto.setCorrecciones("Nuevas correcciones");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort).findById(1L);
        verify(degreeWorkRepositoryPort, times(1)).save(degreeWork); // Solo una vez para ACEPTADO
        assertEquals(EnumEstadoDocument.ACEPTADO, formatoA.getEstado());
        assertEquals("Nuevas correcciones", degreeWork.getCorrecciones());
        assertEquals(LocalDate.now(), formatoA.getFechaActual());
    }

    @Test
    void testActualizarDesdeEvaluacion_WithNoAprobadoState() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setEstado("NO_ACEPTADO");
        dto.setCorrecciones("Documento no cumple requisitos");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort, times(2)).save(degreeWork); // DOS veces para NO_ACEPTADO
        assertEquals(EnumEstadoDocument.NO_ACEPTADO, formatoA.getEstado());
        assertEquals(1, degreeWork.getNoAprobadoCount());
    }

    @Test
    void testActualizarDesdeEvaluacion_OnlyCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setCorrecciones("Solo correcciones sin cambiar estado");

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort).save(degreeWork);
        assertEquals("Solo correcciones sin cambiar estado", degreeWork.getCorrecciones());
        // El estado del documento no debe cambiar
        assertEquals(EnumEstadoDocument.PRIMERA_REVISION, formatoA.getEstado());
    }

    @Test
    void testActualizarDesdeEvaluacion_NoChanges() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        // Sin estado ni correcciones

        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort, never()).save(any(DegreeWork.class));
        // No debería haber cambios
        assertEquals(EnumEstadoDocument.PRIMERA_REVISION, formatoA.getEstado());
        assertEquals("Correcciones iniciales", degreeWork.getCorrecciones());
    }

    @Test
    void testActualizarDesdeEvaluacion_WithCartaAceptacionAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setEstado("RECHAZADO"); // Estado que incrementa contador

        // Configurar múltiples documentos (la carta de aceptación debería ser el último)
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        degreeWork.setAnteproyectos(Collections.singletonList(anteproyecto));
        degreeWork.setCartasAceptacion(Collections.singletonList(cartaAceptacion));

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort, times(2)).save(degreeWork); // DOS veces para RECHAZADO
        // Debería actualizar la carta de aceptación (último documento)
        assertEquals(EnumEstadoDocument.RECHAZADO, cartaAceptacion.getEstado());
        // Los otros documentos no deberían cambiar
        assertEquals(EnumEstadoDocument.PRIMERA_REVISION, formatoA.getEstado());
        assertEquals(EnumEstadoDocument.SEGUNDA_REVISION, anteproyecto.getEstado());
        assertEquals(1, degreeWork.getNoAprobadoCount()); // Contador incrementado
    }

    @Test
    void testActualizarDesdeEvaluacion_WithAnteproyectoAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setEstado("ACEPTADO"); // Estado que NO incrementa contador

        // Configurar solo formato A y anteproyecto (anteproyecto debería ser el último)
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        degreeWork.setAnteproyectos(Collections.singletonList(anteproyecto));
        // Sin cartas de aceptación

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort, times(1)).save(degreeWork); // UNA vez para ACEPTADO
        // Debería actualizar el anteproyecto (último documento disponible)
        assertEquals(EnumEstadoDocument.ACEPTADO, anteproyecto.getEstado());
        // El formato A no debería cambiar
        assertEquals(EnumEstadoDocument.PRIMERA_REVISION, formatoA.getEstado());
        assertEquals(0, degreeWork.getNoAprobadoCount()); // Contador NO incrementado
    }

    @Test
    void testActualizarDesdeEvaluacion_WithFormatoAAsLastDocument() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setEstado("SEGUNDA_REVISION"); // Estado que SÍ incrementa contador (porque no es ACEPTADO)

        // Configurar solo formato A
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        // Sin anteproyectos ni cartas de aceptación

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort, times(2)).save(degreeWork); // DOS veces para SEGUNDA_REVISION
        // Debería actualizar el formato A (único documento disponible)
        assertEquals(EnumEstadoDocument.SEGUNDA_REVISION, formatoA.getEstado());
        assertEquals(1, degreeWork.getNoAprobadoCount()); // Contador SÍ incrementado
    }

    @Test
    void testActualizarDesdeEvaluacion_NoDocuments_OnlyCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
        dto.setCorrecciones("Correcciones sin documentos");

        // Sin documentos
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

        // Act
        degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto);

        // Assert
        verify(degreeWorkRepositoryPort).save(degreeWork);
        assertEquals("Correcciones sin documentos", degreeWork.getCorrecciones());
    }

    @Test
    void testActualizarDesdeEvaluacion_NoDocuments_NoCorrecciones() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(1);
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
        dto.setDegreeWorkId(1);
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
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(null));
        
        assertEquals("El DTO recibido desde Evaluaciones es inválido.", exception.getMessage());
    }

    @Test
    void testActualizarDesdeEvaluacion_NullDegreeWorkId() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> degreeWorkEvaluationUseCase.actualizarDesdeEvaluacion(dto));
        
        assertEquals("El DTO recibido desde Evaluaciones es inválido.", exception.getMessage());
    }

    @Test
    void testActualizarDesdeEvaluacion_DegreeWorkNotFound() {
        // Arrange
        DegreeWorkUpdateDTO dto = new DegreeWorkUpdateDTO();
        dto.setDegreeWorkId(999);

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
        verify(degreeWorkRepositoryPort).save(degreeWork);
        assertEquals(2, degreeWork.getEvaluadores().size());
        assertEquals("evaluador1@unicauca.edu.co", degreeWork.getEvaluadores().get(0).getEmail());
        assertEquals("evaluador2@unicauca.edu.co", degreeWork.getEvaluadores().get(1).getEmail());
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

    // Método auxiliar para probar obtenerUltimoDocumento (si es necesario hacerlo público para testing)
    // Si el método es privado, podemos testearlo indirectamente a través de los otros tests
}