package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private final NavigationController navigation;

    public LoginController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
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
            //Envia login al microservicio
            User loginRequest = new User();
            loginRequest.setEmail(usuario);
            loginRequest.setPassword(contrasenia);

            ResponseEntity<User> response = apiService.post("api/usuarios", "/login", loginRequest, User.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User usuarioLogueado = response.getBody();
                
                //Verifica estado del coordinador (si aplica)
                if ("COORDINATOR".equalsIgnoreCase(usuarioLogueado.getRole())) {
                    if ("PENDIENTE".equals(usuarioLogueado.getStatus())) {
                        mostrarAlerta("Solicitud en espera",
                                "Su solicitud de registro como coordinador aún está en revisión.",
                                Alert.AlertType.INFORMATION);
                        return;
                    } else if ("RECHAZADO".equals(usuarioLogueado.getStatus())) {
                        mostrarAlerta("Solicitud rechazada",
                                "Su solicitud de registro como coordinador fue rechazada.",
                                Alert.AlertType.ERROR);
                        return;
                    }
                }
                if("ADMIN".equalsIgnoreCase(usuarioLogueado.getRole())) {
                    navigation.showHomeAdmin(usuarioLogueado);
                } else {
                    navigation.showHomeWithUser(usuarioLogueado);
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