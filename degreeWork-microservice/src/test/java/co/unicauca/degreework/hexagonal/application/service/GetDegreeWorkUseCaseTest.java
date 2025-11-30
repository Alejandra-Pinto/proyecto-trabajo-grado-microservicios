package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDegreeWorkUseCaseTest {

    @Mock
    private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    private GetDegreeWorkUseCase getDegreeWorkUseCase;

    private DegreeWork degreeWork1;
    private DegreeWork degreeWork2;
    private DegreeWork degreeWork3;

    @BeforeEach
    void setUp() {
        getDegreeWorkUseCase = new GetDegreeWorkUseCase(degreeWorkRepositoryPort);

        // Configurar datos de prueba
        degreeWork1 = new DegreeWork();
        degreeWork1.setId(1L);
        degreeWork1.setEstado(EnumEstadoDegreeWork.FORMATO_A);

        degreeWork2 = new DegreeWork();
        degreeWork2.setId(2L);
        degreeWork2.setEstado(EnumEstadoDegreeWork.ANTEPROYECTO);

        degreeWork3 = new DegreeWork();
        degreeWork3.setId(3L);
        degreeWork3.setEstado(EnumEstadoDegreeWork.MONOGRAFIA);
    }

    @Test
    void testFindById_Successful() {
        // Arrange
        Long degreeWorkId = 1L;
        when(degreeWorkRepositoryPort.findById(degreeWorkId))
                .thenReturn(Optional.of(degreeWork1));

        // Act
        Optional<DegreeWork> result = getDegreeWorkUseCase.findById(degreeWorkId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(degreeWork1, result.get());
        assertEquals(1L, result.get().getId());
        verify(degreeWorkRepositoryPort).findById(degreeWorkId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(degreeWorkRepositoryPort.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act
        Optional<DegreeWork> result = getDegreeWorkUseCase.findById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
        verify(degreeWorkRepositoryPort).findById(nonExistentId);
    }

    @Test
    void testFindById_NullId() {
        // Arrange
        when(degreeWorkRepositoryPort.findById(null))
                .thenReturn(Optional.empty());

        // Act
        Optional<DegreeWork> result = getDegreeWorkUseCase.findById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(degreeWorkRepositoryPort).findById(null);
    }

    @Test
    void testFindAll_Successful() {
        // Arrange
        List<DegreeWork> expectedList = Arrays.asList(degreeWork1, degreeWork2, degreeWork3);
        when(degreeWorkRepositoryPort.findAll())
                .thenReturn(expectedList);

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedList, result);
        verify(degreeWorkRepositoryPort).findAll();
    }

    @Test
    void testFindAll_EmptyList() {
        // Arrange
        when(degreeWorkRepositoryPort.findAll())
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).findAll();
    }

    @Test
    void testFindByTeacher_Successful() {
        // Arrange
        String teacherEmail = "profesor@unicauca.edu.co";
        List<DegreeWork> expectedList = Arrays.asList(degreeWork1, degreeWork2);
        
        when(degreeWorkRepositoryPort.listByTeacher(teacherEmail))
                .thenReturn(expectedList);

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByTeacher(teacherEmail);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedList, result);
        verify(degreeWorkRepositoryPort).listByTeacher(teacherEmail);
    }

    @Test
    void testFindByTeacher_EmptyResult() {
        // Arrange
        String teacherEmail = "noprofesor@unicauca.edu.co";
        when(degreeWorkRepositoryPort.listByTeacher(teacherEmail))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByTeacher(teacherEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByTeacher(teacherEmail);
    }

    @Test
    void testFindByTeacher_NullEmail() {
        // Arrange
        when(degreeWorkRepositoryPort.listByTeacher(null))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByTeacher(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByTeacher(null);
    }

    @Test
    void testFindByTeacher_EmptyEmail() {
        // Arrange
        String emptyEmail = "";
        when(degreeWorkRepositoryPort.listByTeacher(emptyEmail))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByTeacher(emptyEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByTeacher(emptyEmail);
    }

    @Test
    void testFindByStudent_Successful() {
        // Arrange
        String studentEmail = "estudiante@unicauca.edu.co";
        List<DegreeWork> expectedList = Arrays.asList(degreeWork1);
        
        when(degreeWorkRepositoryPort.listByStudent(studentEmail))
                .thenReturn(expectedList);

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByStudent(studentEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList, result);
        verify(degreeWorkRepositoryPort).listByStudent(studentEmail);
    }

    @Test
    void testFindByStudent_EmptyResult() {
        // Arrange
        String studentEmail = "noestudiante@unicauca.edu.co";
        when(degreeWorkRepositoryPort.listByStudent(studentEmail))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByStudent(studentEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByStudent(studentEmail);
    }

    @Test
    void testFindByStudent_NullEmail() {
        // Arrange
        when(degreeWorkRepositoryPort.listByStudent(null))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByStudent(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByStudent(null);
    }

    @Test
    void testFindByStudent_EmptyEmail() {
        // Arrange
        String emptyEmail = "";
        when(degreeWorkRepositoryPort.listByStudent(emptyEmail))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByStudent(emptyEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).listByStudent(emptyEmail);
    }

    @Test
    void testFindByEstado_Successful() {
        // Arrange
        EnumEstadoDegreeWork estado = EnumEstadoDegreeWork.FORMATO_A;
        List<DegreeWork> expectedList = Arrays.asList(degreeWork1);
        
        when(degreeWorkRepositoryPort.findByEstado(estado))
                .thenReturn(expectedList);

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByEstado(estado);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList, result);
        verify(degreeWorkRepositoryPort).findByEstado(estado);
    }

    @Test
    void testFindByEstado_EmptyResult() {
        // Arrange
        // Usar un estado que sabemos que no existe en nuestros datos de prueba
        EnumEstadoDegreeWork estado = EnumEstadoDegreeWork.MONOGRAFIA;
        when(degreeWorkRepositoryPort.findByEstado(estado))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByEstado(estado);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).findByEstado(estado);
    }

    @Test
    void testFindByEstado_NullEstado() {
        // Arrange
        when(degreeWorkRepositoryPort.findByEstado(null))
                .thenReturn(Collections.emptyList());

        // Act
        List<DegreeWork> result = getDegreeWorkUseCase.findByEstado(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(degreeWorkRepositoryPort).findByEstado(null);
    }

    @Test
    void testFindByEstado_AllEstados() {
        // Arrange - Probar con todos los estados del enum correcto
        EnumEstadoDegreeWork[] estados = EnumEstadoDegreeWork.values();
        
        for (EnumEstadoDegreeWork estado : estados) {
            when(degreeWorkRepositoryPort.findByEstado(estado))
                    .thenReturn(Arrays.asList(degreeWork1));
            
            // Act
            List<DegreeWork> result = getDegreeWorkUseCase.findByEstado(estado);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(degreeWorkRepositoryPort).findByEstado(estado);
            
            // Reset mock para el próximo estado
            reset(degreeWorkRepositoryPort);
        }
    }

    @Test
    void testMultipleCalls() {
        // Arrange
        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(degreeWork1));
        when(degreeWorkRepositoryPort.findById(2L)).thenReturn(Optional.of(degreeWork2));
        when(degreeWorkRepositoryPort.findAll()).thenReturn(Arrays.asList(degreeWork1, degreeWork2, degreeWork3));
        when(degreeWorkRepositoryPort.listByTeacher("teacher@unicauca.edu.co")).thenReturn(Arrays.asList(degreeWork1));
        when(degreeWorkRepositoryPort.listByStudent("student@unicauca.edu.co")).thenReturn(Arrays.asList(degreeWork2));
        when(degreeWorkRepositoryPort.findByEstado(EnumEstadoDegreeWork.FORMATO_A)).thenReturn(Arrays.asList(degreeWork1));

        // Act - Ejecutar múltiples consultas
        Optional<DegreeWork> result1 = getDegreeWorkUseCase.findById(1L);
        Optional<DegreeWork> result2 = getDegreeWorkUseCase.findById(2L);
        List<DegreeWork> resultAll = getDegreeWorkUseCase.findAll();
        List<DegreeWork> resultTeacher = getDegreeWorkUseCase.findByTeacher("teacher@unicauca.edu.co");
        List<DegreeWork> resultStudent = getDegreeWorkUseCase.findByStudent("student@unicauca.edu.co");
        List<DegreeWork> resultEstado = getDegreeWorkUseCase.findByEstado(EnumEstadoDegreeWork.FORMATO_A);

        // Assert
        assertTrue(result1.isPresent());
        assertEquals(1L, result1.get().getId());
        
        assertTrue(result2.isPresent());
        assertEquals(2L, result2.get().getId());
        
        assertEquals(3, resultAll.size());
        assertEquals(1, resultTeacher.size());
        assertEquals(1, resultStudent.size());
        assertEquals(1, resultEstado.size());

        // Verificar todas las interacciones
        verify(degreeWorkRepositoryPort).findById(1L);
        verify(degreeWorkRepositoryPort).findById(2L);
        verify(degreeWorkRepositoryPort).findAll();
        verify(degreeWorkRepositoryPort).listByTeacher("teacher@unicauca.edu.co");
        verify(degreeWorkRepositoryPort).listByStudent("student@unicauca.edu.co");
        verify(degreeWorkRepositoryPort).findByEstado(EnumEstadoDegreeWork.FORMATO_A);
    }

    @Test
    void testRepositoryThrowsException() {
        // Arrange
        when(degreeWorkRepositoryPort.findAll())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> getDegreeWorkUseCase.findAll());
        
        assertEquals("Database connection failed", exception.getMessage());
        verify(degreeWorkRepositoryPort).findAll();
    }

    @Test
    void testFindById_WithDifferentIds() {
        // Test con diferentes tipos de IDs
        Long[] testIds = {0L, -1L, Long.MAX_VALUE, 123456789L};
        
        for (Long testId : testIds) {
            // Arrange
            DegreeWork testDegreeWork = new DegreeWork();
            testDegreeWork.setId(testId);
            
            when(degreeWorkRepositoryPort.findById(testId))
                    .thenReturn(Optional.of(testDegreeWork));
            
            // Act
            Optional<DegreeWork> result = getDegreeWorkUseCase.findById(testId);
            
            // Assert
            assertTrue(result.isPresent());
            assertEquals(testId, result.get().getId());
            verify(degreeWorkRepositoryPort).findById(testId);
            
            // Reset mock para el próximo ID
            reset(degreeWorkRepositoryPort);
        }
    }

    @Test
    void testFindByEstado_WithAllValidEstados() {
        // Test específico para verificar que funciona con todos los estados del enum correcto
        assertEquals(3, EnumEstadoDegreeWork.values().length); // FORMATO_A, ANTEPROYECTO, MONOGRAFIA
        
        for (EnumEstadoDegreeWork estado : EnumEstadoDegreeWork.values()) {
            // Arrange
            DegreeWork testWork = new DegreeWork();
            testWork.setId(100L);
            testWork.setEstado(estado);
            
            when(degreeWorkRepositoryPort.findByEstado(estado))
                    .thenReturn(Arrays.asList(testWork));
            
            // Act
            List<DegreeWork> result = getDegreeWorkUseCase.findByEstado(estado);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(estado, result.get(0).getEstado());
            verify(degreeWorkRepositoryPort).findByEstado(estado);
            
            reset(degreeWorkRepositoryPort);
        }
    }
}