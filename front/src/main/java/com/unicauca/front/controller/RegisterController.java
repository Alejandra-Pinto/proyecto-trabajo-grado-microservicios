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
    private RadioButton rbJefeDepartamento; // NUEVO RADIO BUTTON
    @FXML
    private Button btn_register;
    @FXML
    private Hyperlink hpl_login;

    private ToggleGroup groupRoles;
    private final ApiGatewayService apiService;
    private final NavigationController navigation;

    public RegisterController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void onRegister(ActionEvent event) {
        String nombre = txt_nombre.getText().trim();
        String apellido = txt_apellido.getText().trim();
        String programa = cbx_programa.getValue();
        String correo = txt_email.getText().trim();
        String pass = txt_password.getText();
        String confirmPass = txt_confirmPassword.getText();

        //Validaciones básicas
        if (nombre.isEmpty() || apellido.isEmpty() || programa == null ||
            correo.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ||
            groupRoles.getSelectedToggle() == null) {
            mostrarAlerta("Error de registro", "Por favor complete todos los campos y seleccione un rol", Alert.AlertType.WARNING);
            return;
        }

        //Validación de contraseñas
        if (!pass.equals(confirmPass)) {
            mostrarAlerta("Error de registro", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
            return;
        }

        try {
            //Cambio: Crear usuario y enviar al microservicio
            User user = new User();
            user.setFirstName(nombre);
            user.setLastName(apellido);
            user.setProgram(programa);
            user.setEmail(correo);
            user.setPassword(pass);
            user.setStatus("ACEPTADO");
            
            //Cambio: Determinar rol basado en RadioButtons
            if (rbEstudiante.isSelected()) {
                user.setRole("STUDENT");
            } else if (rbDocente.isSelected()) {
                user.setRole("PROFESSOR");
            } else if (rbCoordinador.isSelected()) {
                user.setRole("COORDINATOR");
                user.setStatus("PENDIENTE"); // Coordinadores requieren aprobación
            } else if (rbJefeDepartamento.isSelected()) {
                user.setRole("DEPARTMENT_HEAD");
                user.setStatus("PENDIENTE"); // Jefes de departamento requieren aprobación
            }

            System.out.println("=== DEBUG: Intentando registrar usuario ===");
            System.out.println("Nombre: " + user.getFirstName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Rol: " + user.getRole());
            System.out.println("Estado: " + user.getStatus());

            //Cambio: Enviar registro al microservicio de usuarios
            ResponseEntity<User> response = apiService.post("api/usuarios", "/register", user, User.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User usuarioRegistrado = response.getBody();
                
                String mensajeExito = "Usuario registrado correctamente";
                if ("COORDINATOR".equals(usuarioRegistrado.getRole()) || 
                    "DEPARTMENT_HEAD".equals(usuarioRegistrado.getRole())) {
                    mensajeExito += "\nSu solicitud está en revisión y requiere aprobación";
                }
                
                mostrarAlerta("Registro exitoso", mensajeExito, Alert.AlertType.CONFIRMATION);
                limpiarCampos();
                
                //Cambio: Navegar al login usando NavigationController
                navigation.showLogin();
                
            } else {
                System.out.println("=== DEBUG: Error en respuesta del servidor ===");
                System.out.println("Status Code: " + response.getStatusCode());
                mostrarAlerta("Error de registro", 
                    "No se pudo registrar el usuario. Verifique si el correo ya está en uso.", 
                    Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.out.println("=== DEBUG: Excepción durante el registro ===");
            System.out.println("Tipo de error: " + e.getClass().getName());
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            
            mostrarAlerta("Error de conexión", 
                "No se pudo conectar con el servidor: " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
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
        //Inicializar ComboBox
        cbx_programa.getItems().addAll(
            "Ingeniería de Sistemas",
            "Ingeniería Automática Industrial", 
            "Ingeniería Electrónica y Telecomunicaciones",
            "Técnologo en Telemática"
        );

        //Configurar el ToggleGroup para los roles
        groupRoles = new ToggleGroup();
        rbEstudiante.setToggleGroup(groupRoles);
        rbDocente.setToggleGroup(groupRoles);
        rbCoordinador.setToggleGroup(groupRoles);
        rbJefeDepartamento.setToggleGroup(groupRoles); // NUEVO RADIO BUTTON AL GRUPO
    }
}