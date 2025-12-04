package com.unicauca.front.controller;

import com.unicauca.front.dto.DegreeWorkDTO;
import com.unicauca.front.dto.DocumentDTO;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.EnumEstadoDocument;
import com.unicauca.front.model.EnumTipoDocumento;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ManagementTeacherDraftController {

    // Botones principales
    @FXML private Button btnAdjuntarDocumento;
    @FXML private ToggleButton btnUsuario;
    @FXML private ToggleButton btnFormatoPropuesta;
    @FXML private ToggleButton btnAnteproyecto;

    // Campos de formulario
    @FXML private Label lblTituloTrabajo;
    @FXML private TextField txtArchivoAdjunto;
    @FXML private Button btnAbrirArchivo;
    @FXML private Button btnGuardar;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private File archivoAdjunto;
    private DegreeWork formatoActual;
    private HostServices hostServices;

    public ManagementTeacherDraftController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        cargarAnteproyectoExistente();
    }

    private void configurarInterfaz() {
        if (usuarioActual != null) {
            btnUsuario.setText("Docente: " + usuarioActual.getFirstName());
        }
        
        // Deshabilitar el botón de anteproyecto (ya estamos en esa vista)
        btnAnteproyecto.setDisable(true);
        btnAnteproyecto.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
    }

    private void cargarAnteproyectoExistente() {
        try {
            // Buscar si ya existe un anteproyecto para este docente
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "/docente/" + usuarioActual.getEmail(), 
                DegreeWork[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] degreeWorks = response.getBody();
                if (degreeWorks.length > 0) {
                    // Tomar el primer degreework (asumiendo que un docente tiene solo uno)
                    this.formatoActual = degreeWorks[0];
                    cargarDatosAnteproyecto(formatoActual);
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando anteproyecto existente: " + e.getMessage());
            // No mostrar alerta, puede ser que no exista aún
        }
    }

    private void cargarDatosAnteproyecto(DegreeWork formato) {
        try {
            // Cargar título del proyecto
            lblTituloTrabajo.setText(formato.getTituloProyecto() != null ? 
                formato.getTituloProyecto() : "No hay título disponible");

            // Cargar archivo de anteproyecto
            String archivoAnteproyecto = obtenerArchivoAnteproyecto(formato);
            txtArchivoAdjunto.setText(archivoAnteproyecto);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando anteproyecto: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Método helper para obtener archivo de anteproyecto
    private String obtenerArchivoAnteproyecto(DegreeWork formato) {
        if (formato.getAnteproyectos() != null && !formato.getAnteproyectos().isEmpty()) {
            Document anteproyecto = formato.getAnteproyectos().get(0);
            return anteproyecto.getRutaArchivo() != null ? anteproyecto.getRutaArchivo() : "";
        }
        return "";
    }

    @FXML
    private void onAdjuntarDocumento() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar anteproyecto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Files", "*.docx"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(null);
        if (archivoSeleccionado != null) {
            txtArchivoAdjunto.setText(archivoSeleccionado.getAbsolutePath());
            archivoAdjunto = archivoSeleccionado;
            mostrarAlerta("Documento cargado", "Anteproyecto seleccionado: " + archivoSeleccionado.getName(), Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onAbrirArchivo() {
        String ruta = txtArchivoAdjunto.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Sin archivo", "No hay ningún archivo seleccionado.", Alert.AlertType.WARNING);
            return;
        }
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en la ruta especificada.", Alert.AlertType.ERROR);
            return;
        }
        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
        }
    }

    @FXML
    private void onGuardarFormato() {
        if (!validarCamposObligatorios()) {
            return;
        }

        try {
            if (formatoActual == null) {
                mostrarAlerta("Error", "No se encontró un proyecto asociado.", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("=== DEBUG: INICIANDO ENVÍO DE ANTEPROYECTO ===");
            System.out.println("ID del DegreeWork: " + formatoActual.getId());
            System.out.println("Ruta del archivo: " + txtArchivoAdjunto.getText());

            // 1. CREAR DTO DEL DOCUMENTO DE ANTEPROYECTO
            DocumentDTO anteproyectoDTO = new DocumentDTO();
            anteproyectoDTO.setRutaArchivo(txtArchivoAdjunto.getText());
            anteproyectoDTO.setEstado(EnumEstadoDocument.PRIMERA_REVISION); // Estado inicial por defecto
            anteproyectoDTO.setTipo(EnumTipoDocumento.ANTEPROYECTO);
            
            System.out.println("DocumentDTO creado:");
            System.out.println("  - Ruta: " + anteproyectoDTO.getRutaArchivo());
            System.out.println("  - Estado: " + anteproyectoDTO.getEstado());
            System.out.println("  - Tipo: " + anteproyectoDTO.getTipo());

            // 2. CREAR LISTA DE ANTEPROYECTOS
            List<DocumentDTO> anteproyectosList = new ArrayList<>();
            anteproyectosList.add(anteproyectoDTO);
            
            System.out.println("Lista de anteproyectos creada. Tamaño: " + anteproyectosList.size());

            // 3. CREAR DTO COMPLETO DEL DEGREEWORK
            DegreeWorkDTO dto = new DegreeWorkDTO();
            dto.setId(formatoActual.getId());
            dto.setEstado("ANTEPROYECTO"); // Este es el estado del DegreeWork, no del documento
            
            // Manejar modalidad
            String modalidadStr;
            if (formatoActual.getModalidad() != null) {
                modalidadStr = formatoActual.getModalidad().name();
            } else {
                modalidadStr = "INVESTIGACION";
            }
            dto.setModalidad(modalidadStr);
            
            // Título
            dto.setTitulo(formatoActual.getTituloProyecto() != null ? 
                formatoActual.getTituloProyecto() : "Proyecto de Grado");
                
            // Objetivo general  
            dto.setObjetivoGeneral(formatoActual.getObjetivoGeneral() != null ? 
                formatoActual.getObjetivoGeneral() : "Desarrollar el proyecto de grado según los lineamientos establecidos");
                
            // Fecha actual
            dto.setFechaActual(LocalDate.now());
            
            // ¡¡¡IMPORTANTE!!! AGREGAR EL ANTEPROYECTO AL DTO
            dto.setAnteproyectos(anteproyectosList);
            
            // También podrías necesitar otros campos del DegreeWork existente
            if (formatoActual.getDirectorProyecto() != null) {
                dto.setDirectorEmail(formatoActual.getDirectorProyecto().getEmail());
            }
            /* 
            if (formatoActual.getEstudiantes() != null && !formatoActual.getEstudiantes().isEmpty()) {
                List<String> estudiantesEmails = new ArrayList<>();
                for (User estudiante : formatoActual.getEstudiantes()) {
                    estudiantesEmails.add(estudiante.getEmail());
                }
                dto.setEstudiantesEmails(estudiantesEmails);
            }
*/
            System.out.println("=== DEBUG: DTO COMPLETO A ENVIAR ===");
            System.out.println("  - ID: " + dto.getId());
            System.out.println("  - Estado DegreeWork: " + dto.getEstado());
            System.out.println("  - Modalidad: " + dto.getModalidad());
            System.out.println("  - Título: " + dto.getTitulo());
            System.out.println("  - ¿Tiene anteproyectos?: " + (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()));
            if (dto.getAnteproyectos() != null && !dto.getAnteproyectos().isEmpty()) {
                System.out.println("  - Número de anteproyectos: " + dto.getAnteproyectos().size());
                DocumentDTO primerAnteproyecto = dto.getAnteproyectos().get(0);
                System.out.println("  - Primer anteproyecto - Ruta: " + primerAnteproyecto.getRutaArchivo());
                System.out.println("  - Primer anteproyecto - Estado: " + primerAnteproyecto.getEstado());
                System.out.println("  - Primer anteproyecto - Tipo: " + primerAnteproyecto.getTipo());
            }

            // 4. ENVIAR AL BACKEND
            System.out.println("Enviando PUT a: /api/degreeworks/" + formatoActual.getId());
            ResponseEntity<DegreeWork> response = apiService.put(
                "api/degreeworks", 
                "/" + formatoActual.getId(), 
                dto, 
                DegreeWork.class
            );

            System.out.println("=== DEBUG: RESPUESTA DEL SERVER ===");
            System.out.println("  - Código de estado: " + response.getStatusCode());
            System.out.println("  - ¿Es exitoso?: " + response.getStatusCode().is2xxSuccessful());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork degreeWorkActualizado = response.getBody();
                System.out.println("✅ DegreeWork actualizado recibido:");
                System.out.println("  - ID: " + degreeWorkActualizado.getId());
                System.out.println("  - Estado: " + degreeWorkActualizado.getEstado());
                System.out.println("  - ¿Tiene anteproyectos?: " + (degreeWorkActualizado.getAnteproyectos() != null));
                
                if (degreeWorkActualizado.getAnteproyectos() != null) {
                    System.out.println("  - Número de anteproyectos: " + degreeWorkActualizado.getAnteproyectos().size());
                    if (!degreeWorkActualizado.getAnteproyectos().isEmpty()) {
                        Document ultimoAnteproyecto = degreeWorkActualizado.getAnteproyectos().get(
                            degreeWorkActualizado.getAnteproyectos().size() - 1
                        );
                        System.out.println("  - Último anteproyecto:");
                        System.out.println("    - ID: " + ultimoAnteproyecto.getId());
                        System.out.println("    - Ruta: " + ultimoAnteproyecto.getRutaArchivo());
                        System.out.println("    - Estado: " + ultimoAnteproyecto.getEstado());
                        System.out.println("    - Tipo: " + ultimoAnteproyecto.getTipo());
                    }
                }
                
                mostrarAlerta("Éxito", "Anteproyecto enviado correctamente para revisión", Alert.AlertType.INFORMATION);
                navigation.showHomeWithUser(usuarioActual);
            } else {
                System.out.println("❌ Error en la respuesta del servidor");
                String mensajeError = "No se pudo enviar el anteproyecto. Código: " + response.getStatusCode();
                if (response.getBody() != null) {
                    mensajeError += "\nRespuesta: " + response.getBody().toString();
                }
                mostrarAlerta("Error", mensajeError, Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("=== DEBUG: EXCEPCIÓN DURANTE EL ENVÍO ===");
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCamposObligatorios() {
        if (txtArchivoAdjunto.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Debe adjuntar el documento del anteproyecto", Alert.AlertType.WARNING);
            return false;
        }
        
        // Verificar que el archivo exista
        File archivo = new File(txtArchivoAdjunto.getText());
        if (!archivo.exists() || !archivo.isFile()) {
            mostrarAlerta("Archivo no válido", "El archivo seleccionado no existe o no es válido", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showPublishedTeacherFormatA(usuarioActual);
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            btnUsuario.setText("Docente: " + usuario.getFirstName());
            // Recargar datos con el nuevo usuario
            cargarAnteproyectoExistente();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}