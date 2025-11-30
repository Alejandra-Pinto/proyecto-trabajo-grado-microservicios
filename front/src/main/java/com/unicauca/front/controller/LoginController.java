package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.service.KeycloakService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class LoginController {

    @FXML
    private TextField txt_email;
    @FXML
    private PasswordField txt_password;
    @FXML
    private Button btn_login;
    @FXML
    private Hyperlink hpl_register;

    private final ApiGatewayService apiService;
    private final KeycloakService keycloakService;
    private final NavigationController navigation;

    public LoginController(ApiGatewayService apiService, KeycloakService keycloakService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
        this.keycloakService = keycloakService;
    }

    @FXML
    private void evenBtnIngresar(ActionEvent event) {
        String usuario = txt_email.getText().trim();
        String contrasenia = txt_password.getText().trim();

        if (usuario.isEmpty() || contrasenia.isEmpty()) {
            mostrarAlerta("Error de login", "Por favor llene todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Login con Keycloak
            String token = keycloakService.login(usuario, contrasenia);
            
            if (token != null) {
                // Guardar token en SessionManager
                SessionManager.setAccessToken(token);

                // También en ApiGatewayService (por si acaso)
                apiService.setAccessToken(token);
                
                // Obtener información del usuario desde Keycloak
                Map<String, Object> userInfo = keycloakService.getUserInfoWithRoles(token);
                
                if (userInfo != null) {
                    // Crear objeto User con la información de Keycloak
                    User usuarioLogueado = new User();
                    usuarioLogueado.setEmail((String) userInfo.get("email"));
                    usuarioLogueado.setFirstName((String) userInfo.get("given_name"));
                    usuarioLogueado.setLastName((String) userInfo.get("family_name"));
                    
                    // Obtener rol principal
                    String mainRole = (String) userInfo.get("mainRole");
                    usuarioLogueado.setRole(mainRole != null ? mainRole : "USER");
                    
                    // Mostrar información de debug
                    System.out.println("=== USER LOGIN INFO ===");
                    System.out.println("Email: " + usuarioLogueado.getEmail());
                    System.out.println("Nombre: " + usuarioLogueado.getFirstName() + " " + usuarioLogueado.getLastName());
                    System.out.println("Rol principal: " + usuarioLogueado.getRole());
                    
                    if (userInfo.containsKey("roles")) {
                        List<String> roles = (List<String>) userInfo.get("roles");
                        System.out.println("Todos los roles: " + roles);
                    }
                    
                    if (userInfo.containsKey("client_roles")) {
                        List<String> clientRoles = (List<String>) userInfo.get("client_roles");
                        System.out.println("Roles del cliente: " + clientRoles);
                    }

                    // NUEVO: Verificar estado del usuario en el sistema
                    if (!isUserAllowedToLogin(usuarioLogueado.getEmail(), usuarioLogueado.getRole())) {
                        mostrarAlerta("Acceso no autorizado", 
                                    "Su cuenta está pendiente de aprobación. Por favor, espere a que un administrador active su cuenta.", 
                                    Alert.AlertType.WARNING);
                        // Limpiar sesión
                        SessionManager.clearSession();
                        apiService.setAccessToken(null);
                        return;
                    }
                    
                    // Guardar en sesión
                    SessionManager.setCurrentUser(usuarioLogueado);

                    // Probar una petición autenticada
                    testAuthenticatedRequest();
                    
                    // Navegar según el rol
                    if ("ADMIN".equalsIgnoreCase(usuarioLogueado.getRole())) {
                        navigation.showHomeAdmin(usuarioLogueado);
                    } else {
                        navigation.showHomeWithUser(usuarioLogueado);
                    }
                    
                } else {
                    mostrarAlerta("Error de login", "No se pudo obtener información del usuario.", Alert.AlertType.ERROR);
                }
                
            } else {
                mostrarAlerta("Error de login", "Usuario o contraseña incorrectos.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            mostrarAlerta("Error de conexión", 
                        "No se pudo conectar con el servidor: " + e.getMessage(), 
                        Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Verifica si el usuario tiene permiso para iniciar sesión
     * Solo permitir si el estado es ACEPTADO o ACTIVO, o si es ADMIN
     */
    private boolean isUserAllowedToLogin(String email, String role) {
        try {
            System.out.println("=== VERIFICANDO ESTADO DEL USUARIO ===");
            System.out.println("Email: " + email);
            System.out.println("Rol: " + role);
            
            // EXCEPCIÓN: Los usuarios ADMIN siempre pueden iniciar sesión
            if ("ADMIN".equalsIgnoreCase(role)) {
                return true;
            }
            
            // Obtener información completa del usuario desde el microservicio
            ResponseEntity<User> response = apiService.get("api/usuarios", "/email/" + email, User.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User userFromDb = response.getBody();
                String estado = userFromDb.getStatus();
                
                System.out.println("Estado del usuario: " + estado);
                
                // Definir qué estados permiten login
                boolean allowed = "ACEPTADO".equalsIgnoreCase(estado) || 
                                "ACTIVO".equalsIgnoreCase(estado) ||
                                "ACTIVE".equalsIgnoreCase(estado);
                
                if (!allowed) {
                    System.out.println("❌ Usuario no autorizado - Estado: " + estado);
                }
                
                return allowed;
                
            } else {
                System.out.println("❌ No se pudo obtener información del usuario desde la BD: " + response.getStatusCode());
                // Por seguridad, no permitir acceso si no se puede verificar el estado
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error verificando estado del usuario: " + e.getMessage());
            // Por seguridad, no permitir acceso si hay error
            return false;
        }
    }

    /**
     * Probar que las peticiones autenticadas funcionen
     */
    private void testAuthenticatedRequest() {
        new Thread(() -> {
            try {
                System.out.println("=== TESTING AUTHENTICATED REQUEST ===");
                
                // Probar listar degreeworks (endpoint que requiere autenticación)
                ResponseEntity<String> response = apiService.get("api/degreeworks", "", String.class);
                
                System.out.println("Response Status: " + response.getStatusCode());
                System.out.println("Response Body: " + response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("✅ Petición autenticada EXITOSA");
                } else {
                    System.out.println("❌ Petición autenticada FALLÓ: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                System.out.println("❌ Error en petición autenticada: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void evenBtnRegister(ActionEvent event) {
        navigation.showRegister();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        Label etiqueta = new Label(mensaje);
        etiqueta.setWrapText(true);
        VBox contenedor = new VBox(etiqueta);
        contenedor.setSpacing(10);
        contenedor.setPadding(new javafx.geometry.Insets(10));
        alerta.getDialogPane().setContent(contenedor);

        alerta.showAndWait();
    }

    @FXML
    private void initialize() {
        // El método se ejecuta AUTOMÁTICAMENTE cuando se carga el FXML
        System.out.println("LoginController inicializado");
        
        // Test temporal - eliminar después
        testKeycloakConnection();

        // Verifica que los componentes se cargaron
        if (txt_email == null) System.out.println("ERROR: txt_email es null");
        if (txt_password == null) System.out.println("ERROR: txt_password es null");
        if (btn_login == null) System.out.println("ERROR: btn_login es null");
        if (hpl_register == null) System.out.println("ERROR: hpl_register es null");
    }

    private void testKeycloakConnection() {
        new Thread(() -> {
            try {
                System.out.println("=== TEST KEYCLOAK CONNECTION ===");
                String testToken = keycloakService.login("maria", "1234");
                if (testToken != null) {
                    System.out.println("✅ Keycloak connection SUCCESS");
                    System.out.println("Token length: " + testToken.length());
                } else {
                    System.out.println("❌ Keycloak connection FAILED");
                }
            } catch (Exception e) {
                System.out.println("❌ Keycloak test error: " + e.getMessage());
            }
        }).start();
    }
}