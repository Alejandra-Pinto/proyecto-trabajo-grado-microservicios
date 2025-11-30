package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteDegreeWorkUseCaseTest {

    @Mock
    private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    private DeleteDegreeWorkUseCase deleteDegreeWorkUseCase;

    @BeforeEach
    void setUp() {
        deleteDegreeWorkUseCase = new DeleteDegreeWorkUseCase(degreeWorkRepositoryPort);
    }

    @Test
    void testExecute_SuccessfulDeletion() {
        // Arrange
        Long degreeWorkId = 1L;
        DegreeWork degreeWork = new DegreeWork();
        degreeWork.setId(degreeWorkId);

        when(degreeWorkRepositoryPort.findById(degreeWorkId))
                .thenReturn(Optional.of(degreeWork));
        doNothing().when(degreeWorkRepositoryPort).deleteById(degreeWorkId);

        // Act
        deleteDegreeWorkUseCase.execute(degreeWorkId);

        // Assert
        verify(degreeWorkRepositoryPort).findById(degreeWorkId);
        verify(degreeWorkRepositoryPort).deleteById(degreeWorkId);
        // No exception should be thrown
    }

    @Test
    void testExecute_DegreeWorkNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        
        when(degreeWorkRepositoryPort.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> deleteDegreeWorkUseCase.execute(nonExistentId));
        
        assertEquals("No se encontró el trabajo de grado con ID 999", exception.getMessage());
        
        verify(degreeWorkRepositoryPort).findById(nonExistentId);
        verify(degreeWorkRepositoryPort, never()).deleteById(anyLong());
    }

    @Test
    void testExecute_WithNullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> deleteDegreeWorkUseCase.execute(null));
        
        assertEquals("No se encontró el trabajo de grado con ID null", exception.getMessage());
        
        verify(degreeWorkRepositoryPort).findById(null);
        verify(degreeWorkRepositoryPort, never()).deleteById(anyLong());
    }

    @Test
    void testExecute_VerifyRepositoryInteractionOrder() {
        // Arrange
        Long degreeWorkId = 1L;
        DegreeWork degreeWork = new DegreeWork();
        degreeWork.setId(degreeWorkId);

        when(degreeWorkRepositoryPort.findById(degreeWorkId))
                .thenReturn(Optional.of(degreeWork));
        doNothing().when(degreeWorkRepositoryPort).deleteById(degreeWorkId);

        // Act
        deleteDegreeWorkUseCase.execute(degreeWorkId);

        // Assert - Verify the order of interactions
        var inOrder = inOrder(degreeWorkRepositoryPort);
        inOrder.verify(degreeWorkRepositoryPort).findById(degreeWorkId);
        inOrder.verify(degreeWorkRepositoryPort).deleteById(degreeWorkId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void testExecute_MultipleDeletions() {
        // Arrange
        Long firstId = 1L;
        Long secondId = 2L;
        
        DegreeWork firstDegreeWork = new DegreeWork();
        firstDegreeWork.setId(firstId);
        DegreeWork secondDegreeWork = new DegreeWork();
        secondDegreeWork.setId(secondId);

        when(degreeWorkRepositoryPort.findById(firstId))
                .thenReturn(Optional.of(firstDegreeWork));
        when(degreeWorkRepositoryPort.findById(secondId))
                .thenReturn(Optional.of(secondDegreeWork));
        doNothing().when(degreeWorkRepositoryPort).deleteById(anyLong());

        // Act
        deleteDegreeWorkUseCase.execute(firstId);
        deleteDegreeWorkUseCase.execute(secondId);

        // Assert
        verify(degreeWorkRepositoryPort).findById(firstId);
        verify(degreeWorkRepositoryPort).findById(secondId);
        verify(degreeWorkRepositoryPort).deleteById(firstId);
        verify(degreeWorkRepositoryPort).deleteById(secondId);
        verify(degreeWorkRepositoryPort, times(2)).findById(anyLong());
        verify(degreeWorkRepositoryPort, times(2)).deleteById(anyLong());
    }

    @Test
    void testExecute_WithZeroId() {
        // Arrange
        Long zeroId = 0L;
        DegreeWork degreeWork = new DegreeWork();
        degreeWork.setId(zeroId);

        when(degreeWorkRepositoryPort.findById(zeroId))
                .thenReturn(Optional.of(degreeWork));
        doNothing().when(degreeWorkRepositoryPort).deleteById(zeroId);

        // Act
        deleteDegreeWorkUseCase.execute(zeroId);

        // Assert
        verify(degreeWorkRepositoryPort).findById(zeroId);
        verify(degreeWorkRepositoryPort).deleteById(zeroId);
    }

    @Test
    void testExecute_WithNegativeId() {
        // Arrange
        Long negativeId = -1L;
        
        when(degreeWorkRepositoryPort.findById(negativeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> deleteDegreeWorkUseCase.execute(negativeId));
        
        assertEquals("No se encontró el trabajo de grado con ID -1", exception.getMessage());
        
        verify(degreeWorkRepositoryPort).findById(negativeId);
        verify(degreeWorkRepositoryPort, never()).deleteById(anyLong());
    }

    @Test
    void testExecute_RepositoryThrowsExceptionOnDelete() {
        // Arrange
        Long degreeWorkId = 1L;
        DegreeWork degreeWork = new DegreeWork();
        degreeWork.setId(degreeWorkId);

        when(degreeWorkRepositoryPort.findById(degreeWorkId))
                .thenReturn(Optional.of(degreeWork));
        doThrow(new RuntimeException("Database error"))
                .when(degreeWorkRepositoryPort).deleteById(degreeWorkId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteDegreeWorkUseCase.execute(degreeWorkId));
        
        assertEquals("Database error", exception.getMessage());
        
        verify(degreeWorkRepositoryPort).findById(degreeWorkId);
        verify(degreeWorkRepositoryPort).deleteById(degreeWorkId);
    }

    @Test
    void testExecute_RepositoryThrowsExceptionOnFind() {
        // Arrange
        Long degreeWorkId = 1L;

        when(degreeWorkRepositoryPort.findById(degreeWorkId))
                .thenThrow(new RuntimeException("Connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> deleteDegreeWorkUseCase.execute(degreeWorkId));
        
        assertEquals("Connection error", exception.getMessage());
        
        verify(degreeWorkRepositoryPort).findById(degreeWorkId);
        verify(degreeWorkRepositoryPort, never()).deleteById(anyLong());
    }

    @Test
    void testExecute_WithMaxLongValue() {
        // Arrange
        Long maxId = Long.MAX_VALUE;
        DegreeWork degreeWork = new DegreeWork();
        degreeWork.setId(maxId);

        when(degreeWorkRepositoryPort.findById(maxId))
                .thenReturn(Optional.of(degreeWork));
        doNothing().when(degreeWorkRepositoryPort).deleteById(maxId);

        // Act
        deleteDegreeWorkUseCase.execute(maxId);

        // Assert
        verify(degreeWorkRepositoryPort).findById(maxId);
        verify(degreeWorkRepositoryPort).deleteById(maxId);
    }
}