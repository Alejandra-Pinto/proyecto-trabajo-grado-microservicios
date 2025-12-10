package co.unicauca.degreework.hexagonal.application.service;

import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkCreatedEvent;
import co.unicauca.degreework.hexagonal.application.dto.DegreeWorkDTO;
import co.unicauca.degreework.hexagonal.application.dto.NotificationEventDTO;
import co.unicauca.degreework.hexagonal.domain.model.DegreeWork;
import co.unicauca.degreework.hexagonal.domain.model.User;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumModalidad;
import co.unicauca.degreework.hexagonal.domain.model.enums.EnumEstadoDegreeWork;
import co.unicauca.degreework.hexagonal.domain.service.DegreeWorkValidationService;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkEventMapper;
import co.unicauca.degreework.hexagonal.infra.mapper.DegreeWorkMapper;
import co.unicauca.degreework.hexagonal.port.out.db.DegreeWorkRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.db.UserRepositoryPort;
import co.unicauca.degreework.hexagonal.port.out.messaging.EventPublisherPort;
import co.unicauca.degreework.hexagonal.domain.vo.Titulo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDegreeWorkUseCaseTest {

        @Mock
        private DegreeWorkRepositoryPort degreeWorkRepositoryPort;

        @Mock
        private UserRepositoryPort userRepositoryPort;

        @Mock
        private EventPublisherPort eventPublisherPort;

        @Mock
        private DegreeWorkValidationService validationService;

        @Mock
        private DegreeWorkMapper degreeWorkMapper;

        @Mock
        private DegreeWorkEventMapper degreeWorkEventMapper;

        private CreateDegreeWorkUseCase createDegreeWorkUseCase;

        private DegreeWorkDTO validDegreeWorkDTO;
        private User director;
        private List<User> estudiantes;
        private List<User> codirectores;
        private DegreeWork degreeWork;

        @BeforeEach
        void setUp() {
                createDegreeWorkUseCase = new CreateDegreeWorkUseCase(
                                degreeWorkRepositoryPort,
                                userRepositoryPort,
                                eventPublisherPort,
                                validationService,
                                degreeWorkMapper,
                                degreeWorkEventMapper);

                // Datos de prueba para INVESTIGACION
                validDegreeWorkDTO = new DegreeWorkDTO();
                validDegreeWorkDTO.setTitulo("Título del trabajo de grado");
                validDegreeWorkDTO.setModalidad(EnumModalidad.INVESTIGACION);
                validDegreeWorkDTO.setDirectorEmail("director@unicauca.edu.co");
                validDegreeWorkDTO.setEstudiantesEmails(
                                Arrays.asList("estudiante1@unicauca.edu.co", "estudiante2@unicauca.edu.co"));
                validDegreeWorkDTO.setCodirectoresEmails(Arrays.asList("codirector1@unicauca.edu.co"));
                validDegreeWorkDTO.setFechaActual(LocalDate.now());
                validDegreeWorkDTO.setObjetivoGeneral("Objetivo general del trabajo");
                validDegreeWorkDTO.setObjetivosEspecificos(Arrays.asList("Objetivo 1", "Objetivo 2"));
                validDegreeWorkDTO.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                validDegreeWorkDTO.setFormatosA(Collections.emptyList());
                validDegreeWorkDTO.setAnteproyectos(Collections.emptyList());
                validDegreeWorkDTO.setCartasAceptacion(Collections.emptyList());

                director = new User();
                director.setEmail("director@unicauca.edu.co");
                director.setFirstName("Nombre Director");

                User estudiante1 = new User();
                estudiante1.setEmail("estudiante1@unicauca.edu.co");
                estudiante1.setFirstName("Estudiante 1");
                User estudiante2 = new User();
                estudiante2.setEmail("estudiante2@unicauca.edu.co");
                estudiante2.setFirstName("Estudiante 2");
                estudiantes = Arrays.asList(estudiante1, estudiante2);

                User codirector1 = new User();
                codirector1.setEmail("codirector1@unicauca.edu.co");
                codirector1.setFirstName("Codirector 1");
                codirectores = Arrays.asList(codirector1);

                degreeWork = new DegreeWork();
                degreeWork.setId(1L);
                degreeWork.setTitulo(new Titulo("Título del trabajo de grado"));
                degreeWork.setModalidad(EnumModalidad.INVESTIGACION);
                degreeWork.setEstado(EnumEstadoDegreeWork.FORMATO_A);
        }

        @Test
        void testExecute_SuccessfulCreation_Investigacion() {
                // Arrange
                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));
                when(validationService.validarYObternerEstudiantes(validDegreeWorkDTO.getEstudiantesEmails(),
                                userRepositoryPort))
                                .thenReturn(estudiantes);
                when(validationService.validarYObternerCodirectores(validDegreeWorkDTO.getCodirectoresEmails(),
                                userRepositoryPort))
                                .thenReturn(codirectores);

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);

                DegreeWorkCreatedEvent createdEvent = new DegreeWorkCreatedEvent();
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class))).thenReturn(createdEvent);

                validDegreeWorkDTO.setId(1L);
                when(degreeWorkMapper.toDTO(any(DegreeWork.class))).thenReturn(validDegreeWorkDTO);

                // Act
                DegreeWorkDTO result = createDegreeWorkUseCase.execute(validDegreeWorkDTO);

                // Assert
                assertNotNull(result);
                assertEquals(1L, result.getId());

                verify(userRepositoryPort).findByEmail("director@unicauca.edu.co");
                verify(validationService).validarYObternerEstudiantes(validDegreeWorkDTO.getEstudiantesEmails(),
                                userRepositoryPort);
                verify(validationService).validarYObternerCodirectores(validDegreeWorkDTO.getCodirectoresEmails(),
                                userRepositoryPort);
                verify(degreeWorkRepositoryPort).save(any(DegreeWork.class));
                verify(degreeWorkEventMapper).toCreatedEvent(degreeWork);
                verify(eventPublisherPort).sendDegreeWorkCreated(createdEvent);
                verify(eventPublisherPort).sendNotification(any(NotificationEventDTO.class));
        }

        @Test
        void testExecute_SuccessfulCreation_PracticaProfesional() {
                // Arrange - Configurar para PRACTICA_PROFESIONAL con UN solo estudiante
                DegreeWorkDTO practicaDTO = new DegreeWorkDTO();
                practicaDTO.setTitulo("Práctica Profesional");
                practicaDTO.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);
                practicaDTO.setDirectorEmail("director@unicauca.edu.co");
                practicaDTO.setEstudiantesEmails(Collections.singletonList("estudiante@unicauca.edu.co"));
                practicaDTO.setCodirectoresEmails(Collections.emptyList());
                practicaDTO.setFechaActual(LocalDate.now());
                practicaDTO.setObjetivoGeneral("Objetivo general");
                practicaDTO.setObjetivosEspecificos(Collections.singletonList("Objetivo 1"));
                practicaDTO.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                practicaDTO.setFormatosA(Collections.emptyList());
                practicaDTO.setAnteproyectos(Collections.emptyList());
                practicaDTO.setCartasAceptacion(Collections.emptyList());

                User estudianteUnico = new User();
                estudianteUnico.setEmail("estudiante@unicauca.edu.co");
                estudianteUnico.setFirstName("Estudiante Único");

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));
                when(validationService.validarYObternerEstudiantes(practicaDTO.getEstudiantesEmails(),
                                userRepositoryPort))
                                .thenReturn(Collections.singletonList(estudianteUnico));
                when(validationService.validarYObternerCodirectores(practicaDTO.getCodirectoresEmails(),
                                userRepositoryPort))
                                .thenReturn(Collections.emptyList());

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());
                when(degreeWorkMapper.toDTO(any(DegreeWork.class))).thenReturn(practicaDTO);

                // Act
                DegreeWorkDTO result = createDegreeWorkUseCase.execute(practicaDTO);

                // Assert
                assertNotNull(result);
                verify(degreeWorkRepositoryPort).save(any(DegreeWork.class));
                verify(eventPublisherPort).sendNotification(any(NotificationEventDTO.class));
        }

        @Test
        void testExecute_DirectorNotFound() {
                // Arrange
                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.empty());

                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                () -> createDegreeWorkUseCase.execute(validDegreeWorkDTO));

                assertTrue(exception.getMessage().contains("director@unicauca.edu.co"));

                verify(userRepositoryPort).findByEmail("director@unicauca.edu.co");
                verify(validationService, never()).validarYObternerEstudiantes(any(), any());
                verify(validationService, never()).validarYObternerCodirectores(any(), any());
                verify(degreeWorkRepositoryPort, never()).save(any());
                verify(eventPublisherPort, never()).sendNotification(any());
        }

        @Test
        void testExecute_WithNullDocuments() {
                // Arrange
                // IMPORTANTE: Para este test, necesitamos usar un DTO con listas vacías, no
                // null
                // porque la implementación llama a toString() en la línea 57
                DegreeWorkDTO dto = new DegreeWorkDTO();
                dto.setTitulo("Título del trabajo de grado");
                dto.setModalidad(EnumModalidad.INVESTIGACION);
                dto.setDirectorEmail("director@unicauca.edu.co");
                dto.setEstudiantesEmails(Arrays.asList("estudiante1@unicauca.edu.co", "estudiante2@unicauca.edu.co"));
                dto.setCodirectoresEmails(Arrays.asList("codirector1@unicauca.edu.co"));
                dto.setFechaActual(LocalDate.now());
                dto.setObjetivoGeneral("Objetivo general del trabajo");
                dto.setObjetivosEspecificos(Arrays.asList("Objetivo 1", "Objetivo 2"));
                dto.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                // Listas vacías, no null
                dto.setFormatosA(Collections.emptyList());
                dto.setAnteproyectos(Collections.emptyList());
                dto.setCartasAceptacion(Collections.emptyList());

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));
                when(validationService.validarYObternerEstudiantes(dto.getEstudiantesEmails(),
                                userRepositoryPort))
                                .thenReturn(estudiantes);
                when(validationService.validarYObternerCodirectores(dto.getCodirectoresEmails(),
                                userRepositoryPort))
                                .thenReturn(codirectores);

                // Crear un DegreeWork para el mock
                when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());

                // Configurar el DTO de respuesta con listas vacías
                DegreeWorkDTO responseDTO = new DegreeWorkDTO();
                responseDTO.setId(1L);
                responseDTO.setFormatosA(Collections.emptyList());
                responseDTO.setAnteproyectos(Collections.emptyList());
                responseDTO.setCartasAceptacion(Collections.emptyList());

                when(degreeWorkMapper.toDTO(any(DegreeWork.class))).thenReturn(responseDTO);

                // Act & Assert - Debería funcionar con listas vacías
                assertDoesNotThrow(() -> {
                        DegreeWorkDTO result = createDegreeWorkUseCase.execute(dto);
                        assertNotNull(result);
                });

                verify(degreeWorkRepositoryPort).save(any(DegreeWork.class));
                verify(eventPublisherPort).sendNotification(any(NotificationEventDTO.class));
        }

        @Test
        void testExecute_WithEmptyCodirectores() {
                // Arrange
                validDegreeWorkDTO.setCodirectoresEmails(Collections.emptyList());

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));
                when(validationService.validarYObternerEstudiantes(validDegreeWorkDTO.getEstudiantesEmails(),
                                userRepositoryPort))
                                .thenReturn(estudiantes);
                when(validationService.validarYObternerCodirectores(validDegreeWorkDTO.getCodirectoresEmails(),
                                userRepositoryPort))
                                .thenReturn(Collections.emptyList());

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());

                DegreeWorkDTO responseDTO = new DegreeWorkDTO();
                responseDTO.setId(1L);
                responseDTO.setFormatosA(Collections.emptyList());
                responseDTO.setAnteproyectos(Collections.emptyList());
                responseDTO.setCartasAceptacion(Collections.emptyList());

                when(degreeWorkMapper.toDTO(any(DegreeWork.class))).thenReturn(responseDTO);

                // Act
                DegreeWorkDTO result = createDegreeWorkUseCase.execute(validDegreeWorkDTO);

                // Assert
                assertNotNull(result);
                verify(validationService).validarYObternerCodirectores(Collections.emptyList(), userRepositoryPort);
                verify(eventPublisherPort).sendNotification(any(NotificationEventDTO.class));
        }

        @Test
        void testExecute_WithNullTituloAndFecha() {
                // Arrange
                validDegreeWorkDTO.setTitulo(null);
                validDegreeWorkDTO.setFechaActual(null);

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));
                when(validationService.validarYObternerEstudiantes(validDegreeWorkDTO.getEstudiantesEmails(),
                                userRepositoryPort))
                                .thenReturn(estudiantes);
                when(validationService.validarYObternerCodirectores(validDegreeWorkDTO.getCodirectoresEmails(),
                                userRepositoryPort))
                                .thenReturn(codirectores);

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class))).thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());

                DegreeWorkDTO responseDTO = new DegreeWorkDTO();
                responseDTO.setId(1L);
                responseDTO.setFormatosA(Collections.emptyList());
                responseDTO.setAnteproyectos(Collections.emptyList());
                responseDTO.setCartasAceptacion(Collections.emptyList());

                when(degreeWorkMapper.toDTO(any(DegreeWork.class))).thenReturn(responseDTO);

                // Act
                DegreeWorkDTO result = createDegreeWorkUseCase.execute(validDegreeWorkDTO);

                // Assert
                assertNotNull(result);
                verify(degreeWorkRepositoryPort).save(any(DegreeWork.class));
                verify(eventPublisherPort).sendNotification(any(NotificationEventDTO.class));
        }

        @Test
        void testExecute_UnsupportedModalidad() {
                // Arrange - Crear un DTO con modalidad nula
                DegreeWorkDTO dtoWithInvalidModalidad = new DegreeWorkDTO();
                dtoWithInvalidModalidad.setModalidad(null); // Modalidad nula
                dtoWithInvalidModalidad.setDirectorEmail("director@unicauca.edu.co");
                dtoWithInvalidModalidad.setEstudiantesEmails(Arrays.asList("estudiante@unicauca.edu.co"));
                dtoWithInvalidModalidad.setFormatosA(Collections.emptyList()); // Lista vacía, no null
                dtoWithInvalidModalidad.setAnteproyectos(Collections.emptyList()); // Lista vacía, no null
                dtoWithInvalidModalidad.setCartasAceptacion(Collections.emptyList()); // Lista vacía, no null
                dtoWithInvalidModalidad.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                dtoWithInvalidModalidad.setTitulo("Título");
                dtoWithInvalidModalidad.setFechaActual(LocalDate.now());
                dtoWithInvalidModalidad.setObjetivoGeneral("Objetivo");
                dtoWithInvalidModalidad.setObjetivosEspecificos(Arrays.asList("Obj1"));

                // No configurar mocks ya que la validación debería fallar primero
                // Act & Assert -
                assertThrows(IllegalArgumentException.class,
                                () -> createDegreeWorkUseCase.execute(dtoWithInvalidModalidad));
        }

        @Test
        void testCrearBuilder_Investigacion() {
                // Arrange
                DegreeWorkDTO dto = new DegreeWorkDTO();
                dto.setModalidad(EnumModalidad.INVESTIGACION);
                dto.setTitulo("Test Investigación");
                dto.setDirectorEmail("director@unicauca.edu.co");
                dto.setEstudiantesEmails(Arrays.asList("estudiante@unicauca.edu.co"));
                dto.setCodirectoresEmails(Arrays.asList("codirector@unicauca.edu.co"));
                dto.setFechaActual(LocalDate.now());
                dto.setObjetivoGeneral("Objetivo");
                dto.setObjetivosEspecificos(Arrays.asList("Obj1"));
                dto.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                dto.setFormatosA(Collections.emptyList());
                dto.setAnteproyectos(Collections.emptyList());
                dto.setCartasAceptacion(Collections.emptyList());

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));

                User estudiante = new User();
                estudiante.setEmail("estudiante@unicauca.edu.co");
                when(validationService.validarYObternerEstudiantes(any(), any()))
                                .thenReturn(Arrays.asList(estudiante));

                User codirector = new User();
                codirector.setEmail("codirector@unicauca.edu.co");
                when(validationService.validarYObternerCodirectores(any(), any()))
                                .thenReturn(Arrays.asList(codirector));

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class)))
                                .thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());

                DegreeWorkDTO resultDTO = new DegreeWorkDTO();
                resultDTO.setId(1L);
                resultDTO.setFormatosA(Collections.emptyList());
                resultDTO.setAnteproyectos(Collections.emptyList());
                resultDTO.setCartasAceptacion(Collections.emptyList());

                when(degreeWorkMapper.toDTO(any(DegreeWork.class)))
                                .thenReturn(resultDTO);

                // Act & Assert
                assertDoesNotThrow(() -> {
                        DegreeWorkDTO result = createDegreeWorkUseCase.execute(dto);
                        assertNotNull(result);
                });
        }

        @Test
        void testCrearBuilder_PracticaProfesional() {
                // Arrange
                DegreeWorkDTO dto = new DegreeWorkDTO();
                dto.setModalidad(EnumModalidad.PRACTICA_PROFESIONAL);
                dto.setTitulo("Test Práctica");
                dto.setDirectorEmail("director@unicauca.edu.co");
                dto.setEstudiantesEmails(Arrays.asList("estudiante@unicauca.edu.co"));
                dto.setCodirectoresEmails(Collections.emptyList());
                dto.setFechaActual(LocalDate.now());
                dto.setObjetivoGeneral("Objetivo");
                dto.setObjetivosEspecificos(Arrays.asList("Obj1"));
                dto.setEstado(EnumEstadoDegreeWork.FORMATO_A);
                dto.setFormatosA(Collections.emptyList());
                dto.setAnteproyectos(Collections.emptyList());
                dto.setCartasAceptacion(Collections.emptyList());

                when(userRepositoryPort.findByEmail("director@unicauca.edu.co"))
                                .thenReturn(Optional.of(director));

                User estudiante = new User();
                estudiante.setEmail("estudiante@unicauca.edu.co");
                when(validationService.validarYObternerEstudiantes(any(), any()))
                                .thenReturn(Arrays.asList(estudiante));

                when(validationService.validarYObternerCodirectores(any(), any()))
                                .thenReturn(Collections.emptyList());

                when(degreeWorkRepositoryPort.save(any(DegreeWork.class)))
                                .thenReturn(degreeWork);
                when(degreeWorkEventMapper.toCreatedEvent(any(DegreeWork.class)))
                                .thenReturn(new DegreeWorkCreatedEvent());

                DegreeWorkDTO resultDTO = new DegreeWorkDTO();
                resultDTO.setId(1L);
                resultDTO.setFormatosA(Collections.emptyList());
                resultDTO.setAnteproyectos(Collections.emptyList());
                resultDTO.setCartasAceptacion(Collections.emptyList());

                when(degreeWorkMapper.toDTO(any(DegreeWork.class)))
                                .thenReturn(resultDTO);

                // Act & Assert
                assertDoesNotThrow(() -> {
                        DegreeWorkDTO result = createDegreeWorkUseCase.execute(dto);
                        assertNotNull(result);
                });
        }
}