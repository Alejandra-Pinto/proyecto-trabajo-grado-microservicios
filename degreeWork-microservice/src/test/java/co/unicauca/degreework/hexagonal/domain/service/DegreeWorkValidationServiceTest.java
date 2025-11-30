package co.unicauca.degreework.hexagonal.domain.service;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DegreeWorkValidationServiceTest {

    @Mock
    private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private DegreeWorkValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new DegreeWorkValidationService(degreeWorkRepositoryPort);
    }

    @Test
    void testValidarYObternerEstudiantes_EstudiantesValidos() {
        // Arrange
        List<String> emails = Arrays.asList("estudiante1@unicauca.edu.co", "estudiante2@unicauca.edu.co");
        User estudiante1 = new User();
        estudiante1.setEmail("estudiante1@unicauca.edu.co");
        User estudiante2 = new User();
        estudiante2.setEmail("estudiante2@unicauca.edu.co");

        when(userRepositoryPort.findByEmail("estudiante1@unicauca.edu.co"))
                .thenReturn(Optional.of(estudiante1));
        when(userRepositoryPort.findByEmail("estudiante2@unicauca.edu.co"))
                .thenReturn(Optional.of(estudiante2));
        when(degreeWorkRepositoryPort.listByStudent(anyString()))
                .thenReturn(Collections.emptyList());

        // Act
        List<User> result = validationService.validarYObternerEstudiantes(emails, userRepositoryPort);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("estudiante1@unicauca.edu.co", result.get(0).getEmail());
        assertEquals("estudiante2@unicauca.edu.co", result.get(1).getEmail());
        
        verify(userRepositoryPort, times(2)).findByEmail(anyString());
        verify(degreeWorkRepositoryPort, times(2)).listByStudent(anyString());
    }

    @Test
    void testValidarYObternerEstudiantes_EstudianteNoEncontrado() {
        // Arrange
        List<String> emails = Collections.singletonList("estudiante@unicauca.edu.co");
        when(userRepositoryPort.findByEmail("estudiante@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> validationService.validarYObternerEstudiantes(emails, userRepositoryPort));
        
        assertEquals("No se encontró el estudiante con correo: estudiante@unicauca.edu.co", 
                     exception.getMessage());
        
        verify(userRepositoryPort).findByEmail("estudiante@unicauca.edu.co");
        verify(degreeWorkRepositoryPort, never()).listByStudent(anyString());
    }

    @Test
    void testValidarYObternerEstudiantes_EstudianteConTrabajosActivos() {
        // Arrange
        List<String> emails = Collections.singletonList("estudiante@unicauca.edu.co");
        User estudiante = new User();
        estudiante.setEmail("estudiante@unicauca.edu.co");
        
        DegreeWork trabajoActivo = new DegreeWork();
        Document documentoActivo = new Document();
        documentoActivo.setEstado(EnumEstadoDocument.ACEPTADO); // Estado no rechazado
        trabajoActivo.setFormatosA(Collections.singletonList(documentoActivo));

        when(userRepositoryPort.findByEmail("estudiante@unicauca.edu.co"))
                .thenReturn(Optional.of(estudiante));
        when(degreeWorkRepositoryPort.listByStudent("estudiante@unicauca.edu.co"))
                .thenReturn(Collections.singletonList(trabajoActivo));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> validationService.validarYObternerEstudiantes(emails, userRepositoryPort));
        
        assertEquals("El estudiante con correo estudiante@unicauca.edu.co ya tiene un trabajo con documentos activos (no rechazados) y no puede registrar otro.", 
                     exception.getMessage());
        
        verify(userRepositoryPort).findByEmail("estudiante@unicauca.edu.co");
        verify(degreeWorkRepositoryPort).listByStudent("estudiante@unicauca.edu.co");
    }

    @Test
    void testValidarYObternerEstudiantes_EstudianteConTrabajosSoloRechazados() {
        // Arrange
        List<String> emails = Collections.singletonList("estudiante@unicauca.edu.co");
        User estudiante = new User();
        estudiante.setEmail("estudiante@unicauca.edu.co");
        
        DegreeWork trabajoRechazado = new DegreeWork();
        Document documentoRechazado = new Document();
        documentoRechazado.setEstado(EnumEstadoDocument.RECHAZADO); // Estado rechazado
        trabajoRechazado.setFormatosA(Collections.singletonList(documentoRechazado));

        when(userRepositoryPort.findByEmail("estudiante@unicauca.edu.co"))
                .thenReturn(Optional.of(estudiante));
        when(degreeWorkRepositoryPort.listByStudent("estudiante@unicauca.edu.co"))
                .thenReturn(Collections.singletonList(trabajoRechazado));

        // Act
        List<User> result = validationService.validarYObternerEstudiantes(emails, userRepositoryPort);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("estudiante@unicauca.edu.co", result.get(0).getEmail());
        
        verify(userRepositoryPort).findByEmail("estudiante@unicauca.edu.co");
        verify(degreeWorkRepositoryPort).listByStudent("estudiante@unicauca.edu.co");
    }

    @Test
    void testValidarYObternerCodirectores_CodirectoresValidos() {
        // Arrange
        List<String> emails = Arrays.asList("codirector1@unicauca.edu.co", "codirector2@unicauca.edu.co");
        User codirector1 = new User();
        codirector1.setEmail("codirector1@unicauca.edu.co");
        User codirector2 = new User();
        codirector2.setEmail("codirector2@unicauca.edu.co");

        when(userRepositoryPort.findByEmail("codirector1@unicauca.edu.co"))
                .thenReturn(Optional.of(codirector1));
        when(userRepositoryPort.findByEmail("codirector2@unicauca.edu.co"))
                .thenReturn(Optional.of(codirector2));

        // Act
        List<User> result = validationService.validarYObternerCodirectores(emails, userRepositoryPort);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("codirector1@unicauca.edu.co", result.get(0).getEmail());
        assertEquals("codirector2@unicauca.edu.co", result.get(1).getEmail());
        
        verify(userRepositoryPort, times(2)).findByEmail(anyString());
    }

    @Test
    void testValidarYObternerCodirectores_CodirectorNoEncontrado() {
        // Arrange
        List<String> emails = Collections.singletonList("codirector@unicauca.edu.co");
        when(userRepositoryPort.findByEmail("codirector@unicauca.edu.co"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> validationService.validarYObternerCodirectores(emails, userRepositoryPort));
        
        assertEquals("No se encontró el codirector con correo: codirector@unicauca.edu.co", 
                     exception.getMessage());
        
        verify(userRepositoryPort).findByEmail("codirector@unicauca.edu.co");
    }

    @Test
    void testValidarYObternerCodirectores_ListaNula() {
        // Act
        List<User> result = validationService.validarYObternerCodirectores(null, userRepositoryPort);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort, never()).findByEmail(anyString());
    }

    @Test
    void testValidarYObternerCodirectores_ListaVacia() {
        // Act
        List<User> result = validationService.validarYObternerCodirectores(Collections.emptyList(), userRepositoryPort);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepositoryPort, never()).findByEmail(anyString());
    }

    @Test
    void testTieneDocumentosNoRechazados_ConDocumentosActivos() {
        // Arrange
        DegreeWorkValidationService service = new DegreeWorkValidationService(degreeWorkRepositoryPort);
        DegreeWork degreeWork = new DegreeWork();
        
        Document documentoActivo = new Document();
        documentoActivo.setEstado(EnumEstadoDocument.ACEPTADO);
        
        degreeWork.setFormatosA(Collections.singletonList(documentoActivo));

        // Act
        boolean result = service.tieneDocumentosNoRechazados(degreeWork);

        // Assert
        assertTrue(result);
    }

    @Test
    void testTieneDocumentosNoRechazados_ConDocumentosRechazados() {
        // Arrange
        DegreeWorkValidationService service = new DegreeWorkValidationService(degreeWorkRepositoryPort);
        DegreeWork degreeWork = new DegreeWork();
        
        Document documentoRechazado = new Document();
        documentoRechazado.setEstado(EnumEstadoDocument.RECHAZADO);
        
        degreeWork.setAnteproyectos(Collections.singletonList(documentoRechazado));

        // Act
        boolean result = service.tieneDocumentosNoRechazados(degreeWork);

        // Assert
        assertFalse(result);
    }

    @Test
    void testTieneDocumentosNoRechazados_ConMultiplesDocumentos() {
        // Arrange
        DegreeWorkValidationService service = new DegreeWorkValidationService(degreeWorkRepositoryPort);
        DegreeWork degreeWork = new DegreeWork();
        
        Document documentoRechazado = new Document();
        documentoRechazado.setEstado(EnumEstadoDocument.RECHAZADO);
        
        Document documentoActivo = new Document();
        documentoActivo.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        
        // El último documento es activo (no rechazado)
        degreeWork.setFormatosA(Arrays.asList(documentoRechazado, documentoActivo));

        // Act
        boolean result = service.tieneDocumentosNoRechazados(degreeWork);

        // Assert
        assertTrue(result);
    }

    @Test
    void testTieneDocumentosNoRechazados_SinDocumentos() {
        // Arrange
        DegreeWorkValidationService service = new DegreeWorkValidationService(degreeWorkRepositoryPort);
        DegreeWork degreeWork = new DegreeWork();

        // Act
        boolean result = service.tieneDocumentosNoRechazados(degreeWork);

        // Assert
        assertFalse(result);
    }

    @Test
    void testTieneDocumentosNoRechazados_ConTodosLosTiposDeDocumentos() {
        // Arrange
        DegreeWorkValidationService service = new DegreeWorkValidationService(degreeWorkRepositoryPort);
        DegreeWork degreeWork = new DegreeWork();
        
        Document formatoA = new Document();
        formatoA.setEstado(EnumEstadoDocument.ACEPTADO);
        
        Document anteproyecto = new Document();
        anteproyecto.setEstado(EnumEstadoDocument.RECHAZADO);
        
        Document cartaAceptacion = new Document();
        cartaAceptacion.setEstado(EnumEstadoDocument.SEGUNDA_REVISION);
        
        degreeWork.setFormatosA(Collections.singletonList(formatoA));
        degreeWork.setAnteproyectos(Collections.singletonList(anteproyecto));
        degreeWork.setCartasAceptacion(Collections.singletonList(cartaAceptacion));

        // Act
        boolean result = service.tieneDocumentosNoRechazados(degreeWork);

        // Assert
        assertTrue(result); // Porque el último documento (carta aceptación) no está rechazado
    }
}