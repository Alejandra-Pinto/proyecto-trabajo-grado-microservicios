package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.service.KeycloakService;
import com.unicauca.front.util.NavigationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.regex.Pattern;

@Controller
public class RegisterController {

    @FXML
    private TextField txt_nombre;
    @FXML
    private TextField txt_apellido;
    @FXML
    private ComboBox<String> cbx_programa;
    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField txt_password;
    @FXML
    private PasswordField txt_confirmPassword;
    @FXML
    private RadioButton rbEstudiante;
    @FXML
    private RadioButton rbDocente;
    @FXML
    private RadioButton rbCoordinador;
    @FXML
    private RadioButton rbJefeDepartamento; 
    @FXML
    private Button btn_register;
    @FXML
    private Hyperlink hpl_login;

    private ToggleGroup groupRoles;
    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private final KeycloakService keycloakService;

    // Patrones para validación de contraseña
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

    public RegisterController(ApiGatewayService apiService, NavigationController navigation, KeycloakService keycloakService) {
        this.apiService = apiService;
        this.navigation = navigation;
        this.keycloakService = keycloakService;
    }   

    @FXML
    private void onRegister(ActionEvent event) {
        String nombre = txt_nombre.getText().trim();
        String apellido = txt_apellido.getText().trim();
        String programa = cbx_programa.getValue();
        String correo = txt_email.getText().trim();
        String pass = txt_password.getText();
        String confirmPass = txt_confirmPassword.getText();

        // Validaciones básicas
        if (nombre.isEmpty() || apellido.isEmpty() || programa == null ||
            correo.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ||
            groupRoles.getSelectedToggle() == null) {
            mostrarAlerta("Error de registro", "Por favor complete todos los campos y seleccione un rol", Alert.AlertType.WARNING);
            return;
        }

        // Validación de email
        if (!isValidEmail(correo)) {
            mostrarAlerta("Error de registro", "Por favor ingrese un email válido con dominio @unicauca.edu.co", Alert.AlertType.ERROR);
            return;
        }

        // Validación de contraseñas coincidan
        if (!pass.equals(confirmPass)) {
            mostrarAlerta("Error de registro", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
            return;
        }

        // Validación de fortaleza de contraseña
        String passwordValidation = isValidPassword(pass);
        if (!passwordValidation.equals("VALID")) {
            mostrarAlerta("Contraseña débil", passwordValidation, Alert.AlertType.ERROR);
            return;
        }

        try {
            // Determinar rol basado en RadioButtons
            String role;
            String status = "ACEPTADO";
            
            if (rbEstudiante.isSelected()) {
                role = "STUDENT";
            } else if (rbDocente.isSelected()) {
                role = "PROFESSOR";
            } else if (rbCoordinador.isSelected()) {
                role = "COORDINATOR";
                status = "PENDIENTE";
            } else if (rbJefeDepartamento.isSelected()) {
                role = "DEPARTMENT_HEAD";
                status = "PENDIENTE";
            } else {
                mostrarAlerta("Error de registro", "Por favor seleccione un rol", Alert.AlertType.ERROR);
                return;
            }

            System.out.println("=== REGISTRO CON KEYCLOAK ===");
            System.out.println("Nombre: " + nombre + " " + apellido);
            System.out.println("Email: " + correo);
            System.out.println("Rol Keycloak: " + role);
            System.out.println("Programa: " + programa);

            // PASO 1: Registrar usuario en Keycloak
            boolean keycloakSuccess = keycloakService.registerUser(correo, pass, nombre, apellido, role);
            
            if (!keycloakSuccess) {
                mostrarAlerta("Error de registro", 
                    "No se pudo crear el usuario. El email puede estar en uso.", 
                    Alert.AlertType.ERROR);
                return;
            }

            System.out.println("✅ Usuario creado en Keycloak - procediendo a crear perfil extendido");

            // PASO 2: Crear perfil extendido en el microservicio de usuarios
            User userProfile = new User();
            userProfile.setFirstName(nombre);
            userProfile.setLastName(apellido);
            userProfile.setProgram(programa);
            userProfile.setEmail(correo);
            userProfile.setRole(role);
            userProfile.setStatus(status);

            // Obtener el token de admin de Keycloak y establecerlo en el ApiGatewayService
            String adminToken = keycloakService.getAdminToken();
            apiService.setAccessToken(adminToken);

            // Llamar al endpoint de sync-user con el token de admin
            ResponseEntity<User> response = apiService.post("api/usuarios", "/sync-user", userProfile, User.class, true);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User usuarioRegistrado = response.getBody();
                
                String mensajeExito = "Usuario registrado correctamente";
                if ("COORDINATOR".equals(usuarioRegistrado.getRole()) || 
                    "DEPARTMENT_HEAD".equals(usuarioRegistrado.getRole())) {
                    mensajeExito += "\nSu solicitud está en revisión y requiere aprobación";
                }
                
                mostrarAlerta("Registro exitoso", mensajeExito, Alert.AlertType.CONFIRMATION);
                limpiarCampos();
                
                // Navegar al login
                navigation.showLogin();
                
            } else {
                System.out.println("⚠️ Usuario creado en Keycloak pero error sincronizando perfil: " + response.getStatusCode());
                mostrarAlerta("Registro parcial", 
                    "Usuario creado pero hubo un error sincronizando el perfil. Puede iniciar sesión.", 
                    Alert.AlertType.WARNING);
                limpiarCampos();
                navigation.showLogin();
            }

        } catch (Exception e) {
            System.out.println("❌ Error durante el registro: " + e.getMessage());
            e.printStackTrace();
            
            mostrarAlerta("Error de conexión", 
                "Error durante el registro: " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }

    /**
     * Valida el formato del email
     */
    private boolean isValidEmail(String email) {
        // Validar formato básico de email y dominio unicauca
        return email.matches("^[A-Za-z0-9+_.-]+@unicauca\\.edu\\.co$");
    }

    /**
     * Valida la fortaleza de la contraseña
     * @return "VALID" si es válida, o mensaje de error si no
     */
    private String isValidPassword(String password) {
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        
        if (!HAS_UPPERCASE.matcher(password).find()) {
            return "La contraseña debe contener al menos una letra mayúscula";
        }
        
        if (!HAS_LOWERCASE.matcher(password).find()) {
            return "La contraseña debe contener al menos una letra minúscula";
        }
        
        if (!HAS_DIGIT.matcher(password).find()) {
            return "La contraseña debe contener al menos un número";
        }
        
        if (!HAS_SPECIAL.matcher(password).find()) {
            return "La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{};':\"|,.<>/?).";
        }
        
        return "VALID";
    }

    @FXML
    private void onGoToLogin(ActionEvent event) {
        navigation.showLogin();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        Label etiqueta = new Label(mensaje);
        etiqueta.setWrapText(true);
        etiqueta.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        VBox contenedor = new VBox(etiqueta);
        contenedor.setSpacing(10);
        contenedor.setPadding(new javafx.geometry.Insets(10));

        alerta.getDialogPane().setContent(contenedor);

        alerta.showAndWait();
    }

    private void limpiarCampos() {
        txt_nombre.clear();
        txt_apellido.clear();
        cbx_programa.getSelectionModel().clearSelection();
        txt_email.clear();
        txt_password.clear();
        txt_confirmPassword.clear();
        groupRoles.selectToggle(null);
    }

    @FXML
    private void initialize() {
        // Inicializar ComboBox
        cbx_programa.getItems().addAll(
            "Ingeniería de Sistemas",
            "Ingeniería Automática Industrial", 
            "Ingeniería Electrónica y Telecomunicaciones",
            "Técnologo en Telemática"
        );

        // Configurar el ToggleGroup para los roles
        groupRoles = new ToggleGroup();
        rbEstudiante.setToggleGroup(groupRoles);
        rbDocente.setToggleGroup(groupRoles);
        rbCoordinador.setToggleGroup(groupRoles);
        rbJefeDepartamento.setToggleGroup(groupRoles);

        // Opcional: Agregar listener para mostrar fortaleza de contraseña en tiempo real
        txt_password.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrengthIndicator(newValue);
        });
    }

    /**
     * Opcional: Muestra indicador visual de fortaleza de contraseña
     */
    private void updatePasswordStrengthIndicator(String password) {
        if (password.isEmpty()) {
            txt_password.setStyle("");
            return;
        }
        
        String validation = isValidPassword(password);
        if (validation.equals("VALID")) {
            txt_password.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            txt_password.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
        }
    }
}