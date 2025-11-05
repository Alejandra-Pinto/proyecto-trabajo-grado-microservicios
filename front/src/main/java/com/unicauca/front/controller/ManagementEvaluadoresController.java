package com.unicauca.front.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;

@Controller
public class ManagementEvaluadoresController {

    @FXML private ToggleButton btnRol;
    @FXML private ComboBox<User> comboUsuarios;
    @FXML private Button btnAsignarEvaluador;
    @FXML private TableView<User> tblEvaluadores;
    @FXML private TableColumn<User, String> colNombre;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRolBase;
    @FXML private TableColumn<User, String> colEstado;
    @FXML private Button btnActualizar;
    @FXML private Button btnVolver;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<User> listaEvaluadores;

    public ManagementEvaluadoresController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        cargarUsuariosDisponibles();
        cargarEvaluadoresAsignados();
    }

    private void configurarInterfaz() {
        // Configurar tabla
        colNombre.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()
            ));
        colEmail.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colRolBase.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        colEstado.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // Configurar botones
        btnAsignarEvaluador.setOnAction(e -> asignarEvaluador());
        btnActualizar.setOnAction(e -> {
            cargarUsuariosDisponibles();
            cargarEvaluadoresAsignados();
        });
        btnVolver.setOnAction(e -> navigation.showHomeWithUser(usuarioActual));
        btnRol.setOnAction(e -> navigation.showPersonalInformation(usuarioActual));
    }

    // Agrega estos métodos al controller
    @FXML
    private void onBtnRolClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnCoordinadoresClicked() {
        if (usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementAdmin();
        }
    }

    @FXML
    private void onBtnEvaluadoresClicked() {
        // Ya estamos en esta vista, no hacer nada
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    private void cargarUsuariosDisponibles() {
        try {
            ResponseEntity<User[]> response = apiService.get(
                "api/admin", 
                "/evaluators", 
                User[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User[] usuarios = response.getBody();
                ObservableList<User> usuariosList = FXCollections.observableArrayList(usuarios);
                comboUsuarios.setItems(usuariosList);
                
                // Configurar cómo mostrar los usuarios en el ComboBox
                comboUsuarios.setCellFactory(param -> new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
                        }
                    }
                });
                
                comboUsuarios.setButtonCell(new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
                        }
                    }
                });
                
                System.out.println("Usuarios disponibles cargados: " + usuarios.length);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error cargando usuarios disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarEvaluadoresAsignados() {
        try {
            // Necesitamos un endpoint para obtener los evaluadores ya asignados
            // Por ahora, usaremos el mismo endpoint pero filtramos en frontend
            ResponseEntity<User[]> response = apiService.get(
                "api/usuarios", 
                "/rol/TEACHER", // O el rol que corresponda
                User[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Filtrar usuarios que son evaluadores (aquí necesitarías un campo isEvaluator)
                User[] usuarios = response.getBody();
                listaEvaluadores = FXCollections.observableArrayList(usuarios);
                tblEvaluadores.setItems(listaEvaluadores);
            }
        } catch (Exception e) {
            // Por ahora, tabla vacía
            listaEvaluadores = FXCollections.observableArrayList();
            tblEvaluadores.setItems(listaEvaluadores);
        }
    }

    private void asignarEvaluador() {
        User usuarioSeleccionado = comboUsuarios.getValue();
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Error", "Por favor seleccione un usuario", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Si el usuario no tiene ID, usar el email como identificador
            // O modificar el backend para aceptar email en lugar de ID
            Map<String, Object> dto = new HashMap<>();
            
            if (usuarioSeleccionado.getId() != null) {
                dto.put("userId", usuarioSeleccionado.getId());
            } else {
                // Si no hay ID, usar email (necesitarías modificar el backend)
                dto.put("email", usuarioSeleccionado.getEmail());
            }

            ResponseEntity<String> response = apiService.post(
                "api/admin", 
                "/assign-evaluator", 
                dto, 
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                mostrarAlerta("Éxito", response.getBody(), Alert.AlertType.INFORMATION);
                cargarUsuariosDisponibles();
                cargarEvaluadoresAsignados();
            } else {
                mostrarAlerta("Error", "Error al asignar evaluador: " + response.getBody(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al asignar evaluador: " + e.getMessage(), Alert.AlertType.ERROR);
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