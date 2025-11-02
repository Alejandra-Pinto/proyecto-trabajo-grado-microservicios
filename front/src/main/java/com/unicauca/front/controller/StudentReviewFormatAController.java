package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import org.springframework.stereotype.Controller;

@Controller
public class StudentReviewFormatAController {

    private User usuarioActual;
    private DegreeWork formatoActual;

    @FXML private Label lblTitulo;
    @FXML private Label lblEstadoValor;
    @FXML private TextArea txtCorrecciones;
    @FXML private ToggleButton btnUsuario;
    @FXML private ToggleButton btnFormatoEstudiante;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;

    public StudentReviewFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
    }

    /**
     * Método para recibir el usuario y el formato desde la ventana anterior
     */
    public void setUsuarioYFormato(User usuario, DegreeWork formato) {
        this.usuarioActual = usuario;
        this.formatoActual = formato;
        cargarCorrecciones();
    }

    private void cargarCorrecciones() {
        if (formatoActual == null) {
            txtCorrecciones.setText("No se encontró un formato para este estudiante.");
            return;
        }

        try {
            //Cargar título
            lblTitulo.setText(formatoActual.getTituloProyecto() != null ? 
                formatoActual.getTituloProyecto() : "Sin título");

            //Cargar correcciones
            if (formatoActual.getCorrecciones() != null && !formatoActual.getCorrecciones().trim().isEmpty()) {
                txtCorrecciones.setText(formatoActual.getCorrecciones());
                lblEstadoValor.setText("Con correcciones");
            } else {
                txtCorrecciones.setText("Este trabajo no tiene correcciones registradas.");
                lblEstadoValor.setText("Sin correcciones");
            }

            //Cargar estado si está disponible
            if (formatoActual.getEstado() != null) {
                lblEstadoValor.setText(formatoActual.getEstado().toString() + " - Con correcciones");
            }

            System.out.println("Cargando correcciones para: " + usuarioActual.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar las correcciones: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showUserProfile(usuarioActual);
        }
    }

    @FXML
    private void onBtnFormatoEstudianteClicked() {
        if (usuarioActual != null) {
            //Verificar si es estudiante por el rol
            if (!"STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
                mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
                return;
            }
            navigation.showManagementStudentFormatA(usuarioActual);
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void handleBackToFormatA() {
        if (usuarioActual != null) {
            navigation.showManagementStudentFormatA(usuarioActual);
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
