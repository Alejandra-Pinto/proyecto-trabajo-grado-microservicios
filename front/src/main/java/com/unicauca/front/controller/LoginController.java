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
            // NUEVO: Login con Keycloak
            String token = keycloakService.login(usuario, contrasenia);
            
            if (token != null) {
                // Guardar token en ApiGatewayService
                apiService.setAccessToken(token);
                
                // Obtener información del usuario desde Keycloak
                Map<String, Object> userInfo = keycloakService.getUserInfo(token);
                
                if (userInfo != null) {
                    // Crear objeto User con la información de Keycloak
                    User usuarioLogueado = new User();
                    usuarioLogueado.setEmail(usuario);
                    usuarioLogueado.setFirstName((String) userInfo.get("given_name"));
                    usuarioLogueado.setLastName((String) userInfo.get("family_name"));
                    
                    // Obtener rol desde los realm_access roles de Keycloak
                    // Esto requiere configuración adicional en Keycloak
                    usuarioLogueado.setRole("USER"); // Temporal - lo mejoraremos
                    
                    // Guardar en sesión
                    SessionManager.setCurrentUser(usuarioLogueado);
                    
                    // Navegar según el rol
                    // TEMPORAL: Por ahora todos van al home normal
                    navigation.showHomeWithUser(usuarioLogueado);
                    
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
        }
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
        //El método se ejecuta AUTOMÁTICAMENTE cuando se carga el FXML
        System.out.println("LoginController inicializado");
        
        //Verifica que los componentes se cargaron
        if (txt_email == null) System.out.println("ERROR: txt_email es null");
        if (txt_password == null) System.out.println("ERROR: txt_password es null");
        if (btn_login == null) System.out.println("ERROR: btn_login es null");
        if (hpl_register == null) System.out.println("ERROR: hpl_register es null");
    }
}