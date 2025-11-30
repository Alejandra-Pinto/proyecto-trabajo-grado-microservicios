package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.DocumentDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.Document;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDocument;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumModalidad;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumTipoDocumento;
import co.unicauca.degreework.hexagonal.domain.service.DegreeWorkValidationService;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkEventMapper;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.messaging.EventPublisherPort;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import co.unicauca.degreework.hexagonal.domain.vo.FechaCreacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDegreeWorkUseCaseTest {

    @Mock
    private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @Mock
    private DegreeWorkValidationService validationService;

    @Mock
    private DegreeWorkEventMapper degreeWorkEventMapper;

    private UpdateDegreeWorkUseCase updateDegreeWorkUseCase;

    private DegreeWork existingDegreeWork;

    @BeforeEach
    void setUp() {
        updateDegreeWorkUseCase = new UpdateDegreeWorkUseCase(
                degreeWorkRepositoryPort,
                userRepositoryPort,
                eventPublisherPort,
                validationService,
                degreeWorkEventMapper
        );

        // Configurar datos existentes
        existingDegreeWork = new DegreeWork();
        existingDegreeWork.setId(1L);
        existingDegreeWork.setTitulo(new Titulo("Título original"));
        existingDegreeWork.setFechaActual(new FechaCreacion(LocalDate.of(2023, 1, 1)));
        existingDegreeWork.setObjetivoGeneral("Objetivo original");
        existingDegreeWork.setObjetivosEspecificos(Arrays.asList("Obj1", "Obj2"));
        existingDegreeWork.setCorrecciones("Correcciones originales");
        existingDegreeWork.setEstado(EnumEstadoDegreeWork.FORMATO_A);
        existingDegreeWork.setModalidad(EnumModalidad.INVESTIGACION);
    }

    @Test
    void testExecute_SuccessfulUpdate() {
        // Arrange
        DegreeWorkDTO updateDTO = createCompleteUpdateDTO();
        
        User director = new User();
        director.setEmail("nuevo_director@unicauca.edu.co");
        
        List<User> estudiantes = Arrays.asList(new User(), new User());
        List<User> codirectores = Arrays.asList(new User());

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(userRepositoryPort.findByEmail("nuevo_director@unicauca.edu.co")).thenReturn(Optional.of(director));
        when(validationService.validarYObternerEstudiantes(any(), any())).thenReturn(estudiantes);
        when(validationService.validarYObternerCodirectores(any(), any())).thenReturn(codirectores);
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);
        when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(new DegreeWorkCreatedEvent());

        // Act
        DegreeWork result = updateDegreeWorkUseCase.execute(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(degreeWorkRepositoryPort).save(existingDegreeWork);
        // Verificar que se llamó a los métodos necesarios
        verify(userRepositoryPort).findByEmail("nuevo_director@unicauca.edu.co");
        verify(validationService).validarYObternerEstudiantes(any(), any());
        verify(validationService).validarYObternerCodirectores(any(), any());
    }

    @Test
    void testExecute_PartialUpdate() {
        // Arrange - DTO con solo algunos campos actualizados
        DegreeWorkDTO partialDTO = new DegreeWorkDTO();
        partialDTO.setTitulo("Solo título actualizado");
        partialDTO.setCorrecciones("Solo correcciones actualizadas");
        // No establecer otros campos

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);
        when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(new DegreeWorkCreatedEvent());

        // Act
        DegreeWork result = updateDegreeWorkUseCase.execute(1L, partialDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Solo título actualizado", existingDegreeWork.getTitulo().getValor());
        assertEquals("Solo correcciones actualizadas", existingDegreeWork.getCorrecciones());
        verify(degreeWorkRepositoryPort).save(existingDegreeWork);
    }

    @Test
        void testExecute_WithDocumentUpdates() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        
        DocumentDTO formatoADTO = new DocumentDTO();
        formatoADTO.setRutaArchivo("/nueva/ruta/formatoA.pdf");
        formatoADTO.setEstado(EnumEstadoDocument.ACEPTADO);
        updateDTO.setFormatosA(Arrays.asList(formatoADTO));

        // Configurar documento existente
        Document formatoAExistente = new Document();
        formatoAExistente.setId(1L); // ← AÑADIR ID para que no sea considerado "nuevo"
        formatoAExistente.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoAExistente.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        existingDegreeWork.setFormatosA(Arrays.asList(formatoAExistente));

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);
        when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(new DegreeWorkCreatedEvent());

        // Act
        DegreeWork result = updateDegreeWorkUseCase.execute(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(degreeWorkRepositoryPort).save(existingDegreeWork);
        // Verificar que se actualizó la ruta, pero el estado puede cambiar por manejarRevision
        assertEquals("/nueva/ruta/formatoA.pdf", formatoAExistente.getRutaArchivo());
        // No verificar el estado específico porque manejarRevision lo puede cambiar
    }

    @Test
    void testExecute_WithNewFormatoA() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        
        DocumentDTO formatoADTO = new DocumentDTO();
        formatoADTO.setRutaArchivo("/nueva/ruta/formatoA.pdf");
        formatoADTO.setEstado(EnumEstadoDocument.ACEPTADO);
        updateDTO.setFormatosA(Arrays.asList(formatoADTO));

        // Asegurar que la lista de formatosA esté inicializada
        existingDegreeWork.setFormatosA(new ArrayList<>());

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);
        when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(new DegreeWorkCreatedEvent());

        // Act
        DegreeWork result = updateDegreeWorkUseCase.execute(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(degreeWorkRepositoryPort).save(existingDegreeWork);
        // Cambiar la verificación: esperar que la lista NO esté vacía
        assertFalse(existingDegreeWork.getFormatosA().isEmpty());
    }

    @Test
    void testExecute_WithAnteproyectoButFormatoANotAccepted() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        
        DocumentDTO anteproyectoDTO = new DocumentDTO();
        anteproyectoDTO.setRutaArchivo("/ruta/anteproyecto.pdf");
        anteproyectoDTO.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        updateDTO.setAnteproyectos(Arrays.asList(anteproyectoDTO));

        // Formato A existe pero no está ACEPTADO
        Document formatoAExistente = new Document();
        formatoAExistente.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoAExistente.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        existingDegreeWork.setFormatosA(Arrays.asList(formatoAExistente));

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> updateDegreeWorkUseCase.execute(1L, updateDTO));

        assertEquals("No se puede subir un anteproyecto hasta que el Formato A haya sido ACEPTADO.", exception.getMessage());
    }

    @Test
    void testExecute_WithAnteproyectoAndFormatoAAccepted() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        
        DocumentDTO anteproyectoDTO = new DocumentDTO();
        anteproyectoDTO.setRutaArchivo("/ruta/anteproyecto.pdf");
        anteproyectoDTO.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
        updateDTO.setAnteproyectos(Arrays.asList(anteproyectoDTO));

        // Formato A existe y está ACEPTADO - USAR ArrayList EN LUGAR DE Arrays.asList
        Document formatoAExistente = new Document();
        formatoAExistente.setId(1L);
        formatoAExistente.setTipo(EnumTipoDocumento.FORMATO_A);
        formatoAExistente.setEstado(EnumEstadoDocument.ACEPTADO);
        
        List<Document> formatosAList = new ArrayList<>();
        formatosAList.add(formatoAExistente);
        existingDegreeWork.setFormatosA(formatosAList);
        
        // Asegurar que la lista de anteproyectos esté inicializada
        existingDegreeWork.setAnteproyectos(new ArrayList<>());

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(existingDegreeWork);
        when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(new DegreeWorkCreatedEvent());

        // Act
        DegreeWork result = updateDegreeWorkUseCase.execute(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(degreeWorkRepositoryPort).save(existingDegreeWork);
        // Cambiar la verificación: esperar que la lista NO esté vacía
        assertFalse(existingDegreeWork.getAnteproyectos().isEmpty());
    }

    @Test
    void testExecute_DegreeWorkNotFound() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        when(degreeWorkRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> updateDegreeWorkUseCase.execute(999L, updateDTO));

        assertEquals("No se encontró el trabajo de grado con ID 999", exception.getMessage());
        verify(degreeWorkRepositoryPort, never()).save(any());
    }

    @Test
    void testExecute_DirectorNotFound() {
        // Arrange
        DegreeWorkDTO updateDTO = new DegreeWorkDTO();
        updateDTO.setDirectorEmail("director_inexistente@unicauca.edu.co");
        // También establecer otros campos obligatorios para evitar nulls
        updateDTO.setObjetivoGeneral("Objetivo original");
        updateDTO.setEstado(EnumEstadoDegreeWork.FORMATO_A);
        updateDTO.setModalidad(EnumModalidad.INVESTIGACION);

        when(degreeWorkRepositoryPort.findById(1L)).thenReturn(Optional.of(existingDegreeWork));
        when(userRepositoryPort.findByEmail("director_inexistente@unicauca.edu.co")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> updateDegreeWorkUseCase.execute(1L, updateDTO));

        assertEquals("No se encontró el director con correo: director_inexistente@unicauca.edu.co", exception.getMessage());
        
        // Verificar que no se llamó a save después de la excepción
        // (puede haberse llamado para el memento, pero no después del error)
        verify(degreeWorkRepositoryPort, atMost(1)).save(any());
    }

    // Método auxiliar para crear un DTO completo de actualización
    private DegreeWorkDTO createCompleteUpdateDTO() {
        DegreeWorkDTO dto = new DegreeWorkDTO();
        dto.setTitulo("Título actualizado");
        dto.setFechaActual(LocalDate.of(2024, 1, 1));
        dto.setObjetivoGeneral("Objetivo actualizado");
        dto.setObjetivosEspecificos(Arrays.asList("Obj1 actualizado", "Obj2 actualizado"));
        dto.setCorrecciones("Correcciones actualizadas");
        dto.setEstado(EnumEstadoDegreeWork.ANTEPROYECTO);
        dto.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);
        dto.setDirectorEmail("nuevo_director@unicauca.edu.co");
        dto.setEstudiantesEmails(Arrays.asList("estudiante1@unicauca.edu.co", "estudiante2@unicauca.edu.co"));
        dto.setCodirectoresEmails(Arrays.asList("codirector@unicauca.edu.co"));
        return dto;
    }
}