package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PersonalInformationController {

    @FXML private Label lblTipo;
    @FXML private Label lblNombre;
    @FXML private Label lblEmail;
    @FXML private Label lblPrograma;
    @FXML private Label lblRol;
    @FXML private Label lblTelefono;
    @FXML private Label lblEstado;

    //Botones de navegación
    @FXML private ToggleButton btnFormatoDocente;
    @FXML private ToggleButton btnAnteproyectoDocente;
    @FXML private ToggleButton btnFormatoEstudiante;
    @FXML private ToggleButton btnAnteproyectoEstudiante;
    @FXML private ToggleButton btnEvaluarPropuestas;
    @FXML private ToggleButton btnEvaluarAnteproyectos;
    @FXML private ToggleButton btnCoordinadores;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;

    public PersonalInformationController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

@FXML
private void initialize() {
    System.out.println("=== INICIALIZANDO PersonalInformationController ===");
    
    usuarioActual = SessionManager.getCurrentUser();
    if (usuarioActual != null) {
        System.out.println("📋 Usuario en sesión: " + usuarioActual.getEmail());
        System.out.println("📋 Programa en sesión: " + usuarioActual.getProgram());
        
        actualizarInformacionUsuario();
        cargarInformacionUsuario();
        configurarBotonesPorRol();
    } else {
        System.out.println("❌ ERROR: usuarioActual es null en initialize()");
    }
}

private void actualizarInformacionUsuario() {
    try {
        String email = usuarioActual.getEmail();
        if (email != null && !email.isEmpty()) {
            System.out.println("🔄 Actualizando información para: " + email);
            
            ResponseEntity<User> response = apiService.get("api/usuarios", "/email/" + email, User.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User updatedUser = response.getBody();
                
                System.out.println("📥 Programa desde API: " + updatedUser.getProgram());
                
                // ✅ CORREGIR caracteres para TODOS los programas
                if (updatedUser.getProgram() != null) {
                    String programaCorregido = corregirCaracteresPrograma(updatedUser.getProgram());
                    updatedUser.setProgram(programaCorregido);
                    System.out.println("🔧 Programa corregido: " + programaCorregido);
                }
                
                SessionManager.setCurrentUser(updatedUser);
                this.usuarioActual = updatedUser;
                
                System.out.println("✅ Usuario actualizado en sesión");
            } else {
                System.out.println("❌ Error en respuesta de API: " + response.getStatusCode());
            }
        }
    } catch (Exception e) {
        System.out.println("❌ Error actualizando información del usuario: " + e.getMessage());
        e.printStackTrace();
    }
}



private void cargarInformacionUsuario() {
    // SIEMPRE sincronizar con SessionManager
    this.usuarioActual = SessionManager.getCurrentUser();
    if (usuarioActual == null) return;

    String tipoUsuario = determinarTipoUsuario();
    String nombreCompleto = (usuarioActual.getFirstName() != null ? usuarioActual.getFirstName() : "") + " " + 
                           (usuarioActual.getLastName() != null ? usuarioActual.getLastName() : "");
    
    lblTipo.setText(tipoUsuario);
    lblNombre.setText(nombreCompleto.trim());
    lblEmail.setText(usuarioActual.getEmail() != null ? usuarioActual.getEmail() : "");
    
    // ✅ CORRECCIÓN: Mostrar TODOS los programas, no solo "Sistemas"
    if (usuarioActual.getProgram() != null) {
        if (usuarioActual.getProgram().contains("Sistemas")) {
            lblPrograma.setText("Ingeniería de Sistemas");
        } else {
            // Para otros programas, corregir caracteres y mostrar
            String programaCorregido = corregirCaracteresPrograma(usuarioActual.getProgram());
            lblPrograma.setText(programaCorregido);
        }
    } else {
        lblPrograma.setText("No asignado");
    }
    
    lblRol.setText(usuarioActual.getRole() != null ? usuarioActual.getRole() : "");
    lblTelefono.setText(usuarioActual.getPhone() != null ? usuarioActual.getPhone() : "N/A");
    lblEstado.setText(usuarioActual.getStatus() != null ? usuarioActual.getStatus() : "ACTIVO");
    
    // DEBUG: Verificar qué se está mostrando
    System.out.println("=== INFORMACIÓN CARGADA EN UI ===");
    System.out.println("Programa en UI: " + lblPrograma.getText());
    System.out.println("Email en UI: " + lblEmail.getText());
}

// Método para corregir caracteres de programas
private String corregirCaracteresPrograma(String programa) {
    if (programa == null) return "No asignado";
    
    return programa
        .replace("Ý", "í")
        .replace("ß", "á")
        .replace("Ò", "ó")
        .replace("þ", "ñ")
        .replace("¨", "é")
        .replace("³", "ú");
}

    private String determinarTipoUsuario() {
        if (usuarioActual.getRole() == null) return "Usuario";
        
        switch (usuarioActual.getRole().toUpperCase()) {
            case "STUDENT":
                return "Estudiante";
            case "PROFESSOR":
                return "Docente";
            case "COORDINATOR":
                return "Coordinador";
            case "DEPARTMENT_HEAD": // NUEVO CASO
                return "Jefe de Departamento";
            case "ADMIN":
                return "Administrador";
            default:
                return "Usuario";
        }
    }

    private void configurarBotonesPorRol() {
        //Ocultar todos los botones inicialmente
        btnFormatoDocente.setVisible(false);
        btnAnteproyectoDocente.setVisible(false);
        btnFormatoEstudiante.setVisible(false);
        btnAnteproyectoEstudiante.setVisible(false);
        btnEvaluarPropuestas.setVisible(false);
        btnEvaluarAnteproyectos.setVisible(false);
        btnCoordinadores.setVisible(false);

        if (usuarioActual == null || usuarioActual.getRole() == null) return;

        //Mostrar botones según el rol
        String rol = usuarioActual.getRole().toUpperCase();
        switch (rol) {
            case "PROFESSOR":
                btnFormatoDocente.setVisible(true);
                btnAnteproyectoDocente.setVisible(true);
                break;
            case "STUDENT":
                btnFormatoEstudiante.setVisible(true);
                btnAnteproyectoEstudiante.setVisible(true);
                break;
            case "COORDINATOR":
                btnEvaluarPropuestas.setVisible(true);
                btnEvaluarAnteproyectos.setVisible(true);
                break;
            case "DEPARTMENT_HEAD":
                btnEvaluarPropuestas.setVisible(true);
                btnEvaluarAnteproyectos.setVisible(true);
                break;
            case "ADMIN":
                btnCoordinadores.setVisible(true);
                break;
        }
    }

    //Métodos de navegación
    @FXML
    private void handleVolver() {
        if (usuarioActual != null) {
            if ("ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
                navigation.showHomeAdmin();
            } else {
                navigation.showHomeWithUser(usuarioActual);
            }
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    // En PersonalInformationController.java
    @FXML
    private void onBtnRolClicked() {
        // Navegar de regreso al home
        if (usuarioActual != null) {
            navigation.showHomeWithUser(usuarioActual);
        }
    }
    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showPublishedTeacherFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnAnteproyectoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnFormatoEstudianteClicked() {
        if (usuarioActual != null && "STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementStudentFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnAnteproyectoEstudianteClicked() {
        if (usuarioActual != null && "STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            //Controlador específico para anteproyectos de estudiantes
            navigation.showManagementStudentFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnEvaluarPropuestasClicked() {
        if (usuarioActual != null && 
            ("COORDINATOR".equalsIgnoreCase(usuarioActual.getRole()) || 
            "DEPARTMENT_HEAD".equalsIgnoreCase(usuarioActual.getRole()))) {
            navigation.showManagementCoordinatorFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo coordinadores y jefes de departamento pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnEvaluarAnteproyectosClicked() {
        if (usuarioActual != null && "DEPARTMENT_HEAD".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementCoordinatorFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los coordinadores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnCoordinadoresClicked() {
        if (usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementAdmin();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los administradores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onBtnEditarInformacion() {
        if (usuarioActual != null) {
            //Vista de edición de perfil (por si alcanza el tiempo jaja)
            mostrarAlerta("Funcionalidad en desarrollo", 
                         "La edición de información personal estará disponible próximamente.", 
                         Alert.AlertType.INFORMATION);
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