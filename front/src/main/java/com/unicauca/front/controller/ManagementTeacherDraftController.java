package com.unicauca.front.controller;

import com.unicauca.front.dto.DegreeWorkDTO;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
            // Primero necesitamos obtener el degreework existente
            if (formatoActual == null) {
                mostrarAlerta("Error", "No se encontró un proyecto asociado. Debe tener un Formato A aprobado primero.", Alert.AlertType.ERROR);
                return;
            }

            // Crear DTO para actualizar el anteproyecto
            DegreeWorkDTO dto = new DegreeWorkDTO();
            dto.setId(formatoActual.getId());
            
            // Actualizar estado a ANTEPROYECTO
            dto.setEstado("ANTEPROYECTO");

            // Enviar al microservicio para actualizar
            ResponseEntity<DegreeWork> response = apiService.put(
                "api/degreeworks", 
                "/" + formatoActual.getId(), 
                dto, 
                DegreeWork.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                mostrarAlerta("Éxito", "Anteproyecto enviado correctamente para revisión", Alert.AlertType.INFORMATION);
                navigation.showHomeWithUser(usuarioActual);
            } else {
                mostrarAlerta("Error", "No se pudo enviar el anteproyecto", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCamposObligatorios() {
        if (txtArchivoAdjunto.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos", "Debe adjuntar el documento del anteproyecto", Alert.AlertType.WARNING);
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