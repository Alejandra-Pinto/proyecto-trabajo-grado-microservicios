package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Controller;

@Controller
public class HomeController {

    @FXML
    private SplitPane splitPane;
    @FXML
    private AnchorPane contentPane;
    @FXML
    private ToggleButton btnRol;
    @FXML
    private ToggleButton btnAnteproyectoDocente;
    @FXML
    private ToggleButton btnFormatoDocente;
    @FXML
    private ToggleButton btnFormatoEstudiante;
    @FXML
    private ToggleButton btnAnteproyectoEstudiante;
    @FXML
    private ToggleButton btnEvaluarPropuestas;
    @FXML
    private ToggleButton btnEvaluarAnteproyectos;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuario;

    public HomeController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        configurarBotones();
    }

    //Método para recibir el usuario (similar a setUsuario)
    public void configurarConUsuario(User usuario) {
        this.usuario = usuario;
        cargarUsuario();
    }

    private void cargarUsuario() {
        if (usuario == null) return;

        String programa = usuario.getProgram() != null ? usuario.getProgram() : "Sin programa";
        String rol = usuario.getRole();

        //Usa el campo 'role' en lugar de instanceof
        btnRol.setText(rol + "\n(" + programa + ")");

        if ("PROFESSOR".equalsIgnoreCase(rol)) {
            btnAnteproyectoDocente.setVisible(true);
            btnFormatoDocente.setVisible(true);

        } else if ("STUDENT".equalsIgnoreCase(rol)) {
            btnFormatoEstudiante.setVisible(true);
            btnAnteproyectoEstudiante.setVisible(true);

        } else if ("COORDINATOR".equalsIgnoreCase(rol)) {
            btnEvaluarPropuestas.setVisible(true);
            btnEvaluarAnteproyectos.setVisible(true);
        }
    }
    
    @FXML
    private void onBtnRolClicked() {
        navigation.showPersonalInformation();
    }

    //Logout
    @FXML
    private void handleLogout() {
        navigation.showLogin();
    }

    //Botones de navegación según rol
    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (!"PROFESSOR".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showPublishedTeacherFormatA(usuario);
    }

    @FXML
    private void onBtnFormatoEstudianteClicked() {
        if (!"STUDENT".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementStudentFormatA();
    }

    @FXML
    private void onBtnEvaluarPropuestasClicked() {
        if (!"COORDINATOR".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los coordinadores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementCoordinatorFormatA();
    }

    @FXML
    private void onBtnEvaluarAnteproyectosClicked() {
        if (!"COORDINATOR".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los coordinadores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementCoordinatorFormatA();
    }

    //Métodos Auxiliares
    private void configurarBotones() {
        btnRol.setVisible(true);
        btnAnteproyectoDocente.setVisible(false);
        btnFormatoDocente.setVisible(false);
        btnFormatoEstudiante.setVisible(false);
        btnAnteproyectoEstudiante.setVisible(false);
        btnEvaluarPropuestas.setVisible(false);
        btnEvaluarAnteproyectos.setVisible(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}