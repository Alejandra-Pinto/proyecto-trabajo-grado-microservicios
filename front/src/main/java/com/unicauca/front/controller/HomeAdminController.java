package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import org.springframework.stereotype.Controller;

@Controller
public class HomeAdminController {

    @FXML private ToggleButton btnUsuario;
    @FXML private ToggleButton btnCoordinadores;
    @FXML private ToggleButton btnEvaluadores;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;

    public HomeAdminController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        System.out.println("HomeAdminController inicializado: " + this);
    }

    public void configurarConUsuario(User usuario) {
        System.out.println("configurarConUsuario ejecutado en: " + this + " con usuario: " + usuario);
        this.usuarioActual = usuario;
        if (usuario != null) {
            btnUsuario.setText("Admin: " + usuario.getFirstName());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnCoordinadoresClicked() {
        System.out.println("onBtnCoordinadoresClicked ejecutado. Usuario: " + usuarioActual);
        if (usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
            System.out.println("Abriendo ManagementAdmin...");
            navigation.showManagementAdmin();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los administradores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnEvaluadoresClicked() {
        System.out.println("onBtnEvaluadoresClicked ejecutado. Usuario: " + usuarioActual);
        if (usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
            System.out.println("Abriendo ManagementEvaluadores...");
            navigation.showManagementEvaluadores();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los administradores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToHome() {
        if (usuarioActual != null) {
            navigation.showHomeWithUser(usuarioActual);
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