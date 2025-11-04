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

        } else if ("COORDINATOR".equalsIgnoreCase(rol) || "DEPARTMENT_HEAD".equalsIgnoreCase(rol)) {
            btnEvaluarPropuestas.setVisible(true);
            btnEvaluarAnteproyectos.setVisible(true);
        }
    }
    
    @FXML
    private void onBtnRolClicked() {
        navigation.showPersonalInformation(usuario);
    }

    //Logout
    @FXML
    private void handleLogout() {
        navigation.showLogin();
    }

    //Botones de navegación según rol

    //PROFESOR
    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (!"PROFESSOR".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showPublishedTeacherFormatA(usuario);
    }

    @FXML
    private void onBtnAnteproyectoDocenteClicked() {
        if (!"PROFESSOR".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementTeacherDraft(usuario);
    }

    //ESTUDIANTE
    @FXML
    private void onBtnFormatoEstudianteClicked() {
        if (!"STUDENT".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementStudentFormatA(usuario);
    }

    @FXML
    private void onBtnAnteproyectoEstudianteClicked() {
        if (!"STUDENT".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementStudentDraft(usuario);
    }

    //COORDINADOR Y JEFE DE DEPARTAMENTO
    @FXML
    private void onBtnEvaluarPropuestasClicked() {
        if (!"COORDINATOR".equalsIgnoreCase(usuario.getRole()) && !"DEPARTMENT_HEAD".equalsIgnoreCase(usuario.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los coordinadores y jefes de departamento pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementCoordinatorFormatA(usuario);
    }

    @FXML
    private void onBtnEvaluarAnteproyectosClicked() {
        System.out.println("=== DEBUG: onBtnEvaluarAnteproyectosClicked ===");
        System.out.println("Usuario: " + (usuario != null ? usuario.getEmail() : "null"));
        System.out.println("Rol: " + (usuario != null ? usuario.getRole() : "null"));
        
        if (!"COORDINATOR".equalsIgnoreCase(usuario.getRole()) && !"DEPARTMENT_HEAD".equalsIgnoreCase(usuario.getRole())) {
            System.out.println("DEBUG: Acceso denegado - rol no permitido");
            mostrarAlerta("Acceso denegado", "Solo los coordinadores y jefes de departamento pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        
        // Navegar a la vista correspondiente según el rol
        if ("COORDINATOR".equalsIgnoreCase(usuario.getRole())) {
            System.out.println("DEBUG: Navegando a ManagementCoordinatorFormatA");
            navigation.showManagementCoordinatorFormatA();
        } else if ("DEPARTMENT_HEAD".equalsIgnoreCase(usuario.getRole())) {
            System.out.println("DEBUG: Navegando a PublishedDepartmentHeadDraft");
            navigation.showPublishedDepartmentHeadDraft(usuario);
        } else {
            System.out.println("DEBUG: Rol no manejado: " + usuario.getRole());
        }
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