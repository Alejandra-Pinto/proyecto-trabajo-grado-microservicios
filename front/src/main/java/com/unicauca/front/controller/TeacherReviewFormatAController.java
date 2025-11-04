package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

@Controller
public class TeacherReviewFormatAController {

    private User usuarioActual;
    private DegreeWork formatoActual;

    @FXML private Label lblTitulo;
    @FXML private Label lblEstadoValor;
    @FXML private TextArea txtCorrecciones;
    @FXML private ToggleButton btnUsuario;
    @FXML private ToggleButton btnFormatoPropuesta;
    @FXML private ToggleButton btnAnteproyecto;
    @FXML private Button btnResubir;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;

    public TeacherReviewFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        txtCorrecciones.setEditable(false);
        txtCorrecciones.setWrapText(true);
        
        if (usuarioActual != null) {
            cargarFormatosDelDocente();
        }
    }

    public void setUsuarioYFormato(User usuario, DegreeWork formato) {
        this.usuarioActual = usuario;
        this.formatoActual = formato;
        cargarCorrecciones();
    }

    public void configurarConFormato(DegreeWork formato) {
        this.formatoActual = formato;
        cargarCorrecciones();
    }

    private void cargarCorrecciones() {
        if (formatoActual == null) {
            txtCorrecciones.setText("No se encontró un formato para este docente.");
            return;
        }

        try {
            lblTitulo.setText("Correcciones de: " + 
                (formatoActual.getTituloProyecto() != null ? formatoActual.getTituloProyecto() : "Sin título"));
            
            if (formatoActual.getCorrecciones() != null && !formatoActual.getCorrecciones().trim().isEmpty()) {
                txtCorrecciones.setText(formatoActual.getCorrecciones());
                
                if (formatoActual.getEstado() != null) {
                    lblEstadoValor.setText(formatoActual.getEstado().toString());
                    aplicarColorEstado(formatoActual.getEstado().toString());
                } else {
                    lblEstadoValor.setText("Con correcciones");
                }
            } else {
                txtCorrecciones.setText("Este trabajo no tiene correcciones registradas.");
                lblEstadoValor.setText("Sin correcciones");
                lblEstadoValor.setStyle("-fx-text-fill: #e0e0e0;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando correcciones: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void aplicarColorEstado(String estado) {
        switch (estado.toUpperCase()) {
            case "APROBADO":
            case "ACEPTADO":
                lblEstadoValor.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                break;
            case "RECHAZADO":
            case "NO_ACEPTADO":
                lblEstadoValor.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                break;
            case "CORREGIDO":
            case "REVISADO":
                lblEstadoValor.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                break;
            default:
                lblEstadoValor.setStyle("-fx-text-fill: #e0e0e0; -fx-font-weight: bold;");
                break;
        }
    }

    private void cargarFormatosDelDocente() {
        if (usuarioActual == null || !"PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            return;
        }

        try {
            //Obtener formatos del docente desde microservicio
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "/docente/" + usuarioActual.getEmail(), 
                DegreeWork[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<DegreeWork> formatos = Arrays.asList(response.getBody());
                System.out.println("Formatos del docente cargados: " + formatos.size());
            }

        } catch (Exception e) {
            System.err.println("Error cargando formatos del docente: " + e.getMessage());
            mostrarAlerta("Error", "No se pudieron cargar los formatos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            //Navegar a gestión de propuestas del docente
            navigation.showManagementTeacherFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnAnteproyectoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            //Navegar a gestión de anteproyectos del docente
            navigation.showManagementTeacherFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void onResubirFormato() {
        if (formatoActual == null) {
            mostrarAlerta("Error", "No hay un formato seleccionado para re-subir.", Alert.AlertType.WARNING);
            return;
        }
        
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            //Navegar a ManagementTeacherFormatA con el formato para edición
            navigation.showManagementTeacherFormatAWithFormato(usuarioActual, formatoActual);
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden re-subir formatos.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToHome() {
        if (usuarioActual != null) {
            navigation.showHomeWithUser(usuarioActual);
        }
    }

    @FXML
    private void handleBackToTeacherManagement() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherFormatA();
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