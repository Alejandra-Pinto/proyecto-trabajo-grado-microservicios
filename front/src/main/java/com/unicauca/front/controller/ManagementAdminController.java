package com.unicauca.front.controller;

import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

@Controller
public class ManagementAdminController {

    @FXML
    private TableView<User> tblUsuarios;
    @FXML
    private TableColumn<User, String> colCorreo;
    @FXML
    private TableColumn<User, String> colNombre;
    @FXML
    private TableColumn<User, String> colRol;
    @FXML
    private TableColumn<User, String> colEstado;
    @FXML
    private TableColumn<User, Void> colAcciones;
    @FXML
    private Button btnGuardarCambios;
    @FXML
    private ToggleButton btnUsuario;
    @FXML
    private ToggleButton btnEvaluadores;
    @FXML
    private ToggleButton btnFormatoPropuesta;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<User> usuarios;

    public ManagementAdminController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarTabla();
        cargarUsuarios();
        if (usuarioActual != null && "ADMIN".equalsIgnoreCase(usuarioActual.getRole())) {
            System.out.println("Abriendo ManagementEvaluadores...");
            configurarBotonesAdmin();
        }
    }

    private void configurarTabla() {
        // Columna Correo
        colCorreo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEmail() != null ? data.getValue().getEmail() : ""));

        // Columna Nombre
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getFirstName() != null ? data.getValue().getFirstName() : "") + " " +
                        (data.getValue().getLastName() != null ? data.getValue().getLastName() : "")));

        // NUEVA Columna Rol
        colRol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                obtenerRolDisplay(data.getValue().getRole())));

        // Columna Estado con ComboBox
        colEstado.setCellFactory(new Callback<TableColumn<User, String>, TableCell<User, String>>() {
            @Override
            public TableCell<User, String> call(TableColumn<User, String> param) {
                return new TableCell<User, String>() {
                    private final ComboBox<String> combo = new ComboBox<>();

                    {
                        combo.getItems().addAll("PENDIENTE", "ACEPTADO", "RECHAZADO");
                        combo.setOnAction(e -> {
                            User usuario = getTableView().getItems().get(getIndex());
                            if (usuario != null) {
                                usuario.setStatus(combo.getValue());
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String estado, boolean empty) {
                        super.updateItem(estado, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            User usuario = getTableRow().getItem();
                            combo.setValue(usuario.getStatus() != null ? usuario.getStatus() : "PENDIENTE");
                            setGraphic(combo);
                        }
                    }
                };
            }
        });

        // Columna de acciones
        colAcciones.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button btnEliminar = new Button("Eliminar");

                    {
                        btnEliminar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btnEliminar.setOnAction(event -> {
                            User usuario = getTableView().getItems().get(getIndex());
                            if (usuario != null) {
                                eliminarUsuario(usuario);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnEliminar);
                        }
                    }
                };
            }
        });
    }

    // Método para obtener el nombre display del rol
    private String obtenerRolDisplay(String rol) {
        if (rol == null)
            return "Sin rol";
        switch (rol.toUpperCase()) {
            case "COORDINATOR":
                return "Coordinador";
            case "DEPARTMENT_HEAD":
                return "Jefe de Departamento";
            case "PROFESSOR":
                return "Docente";
            case "STUDENT":
                return "Estudiante";
            case "ADMIN":
                return "Administrador";
            default:
                return rol;
        }
    }

    private void cargarUsuarios() {
        try {
            // Cargar coordinadores
            ResponseEntity<User[]> responseCoordinadores = apiService.get(
                    "api/usuarios",
                    "/rol/COORDINATOR",
                    User[].class);

            // Cargar jefes de departamento
            ResponseEntity<User[]> responseJefesDepartamento = apiService.get(
                    "api/usuarios",
                    "/rol/DEPARTMENT_HEAD",
                    User[].class);

            usuarios = FXCollections.observableArrayList();

            // Agregar coordinadores a la lista
            if (responseCoordinadores.getStatusCode().is2xxSuccessful() && responseCoordinadores.getBody() != null) {
                usuarios.addAll(Arrays.asList(responseCoordinadores.getBody()));
                System.out.println("Coordinadores cargados: " + responseCoordinadores.getBody().length);
            }

            // Agregar jefes de departamento a la lista
            if (responseJefesDepartamento.getStatusCode().is2xxSuccessful()
                    && responseJefesDepartamento.getBody() != null) {
                usuarios.addAll(Arrays.asList(responseJefesDepartamento.getBody()));
                System.out.println("Jefes de Departamento cargados: " + responseJefesDepartamento.getBody().length);
            }

            tblUsuarios.setItems(usuarios);
            System.out.println("Total de usuarios cargados: " + usuarios.size());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando usuarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onGuardarCambios() {
        if (usuarios == null || usuarios.isEmpty()) {
            mostrarAlerta("Información", "No hay usuarios para actualizar.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            boolean huboCambios = false;
            int cambiosExitosos = 0;

            for (User usuario : usuarios) {
                // Solo procesar usuarios cuyo estado haya cambiado
                String estadoActual = usuario.getStatus();

                if ("ACEPTADO".equals(estadoActual)) {
                    // Usar el endpoint correcto para aprobar
                    ResponseEntity<String> response = apiService.put(
                            "api/admin",
                            "/approve/" + usuario.getEmail(),
                            null,
                            String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        cambiosExitosos++;
                        huboCambios = true;
                        System.out.println("Usuario aprobado: " + usuario.getEmail() + " - Rol: " + usuario.getRole());
                    }

                } else if ("RECHAZADO".equals(estadoActual)) {
                    // Usar el endpoint correcto para rechazar
                    ResponseEntity<String> response = apiService.put(
                            "api/admin",
                            "/reject/" + usuario.getEmail(),
                            null,
                            String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        cambiosExitosos++;
                        huboCambios = true;
                        System.out.println("Usuario rechazado: " + usuario.getEmail() + " - Rol: " + usuario.getRole());
                    }
                }
                // Los que están en "PENDIENTE" no se procesan
            }

            if (huboCambios) {
                mostrarAlerta("Éxito",
                        cambiosExitosos + " usuario(s) actualizado(s) correctamente.",
                        Alert.AlertType.INFORMATION);
                // Recargar datos para verificar cambios
                cargarUsuarios();
            } else {
                mostrarAlerta("Aviso", "No se realizaron cambios en los estados.", Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error actualizando usuarios: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarUsuario(User usuario) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar al usuario?");
        confirmacion
                .setContentText("Usuario: " + usuario.getEmail() + "\nRol: " + obtenerRolDisplay(usuario.getRole()));

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Cambiar el estado a RECHAZADO
                    ResponseEntity<String> deleteResponse = apiService.put(
                            "api/admin",
                            "/reject/" + usuario.getEmail(),
                            null,
                            String.class);

                    if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                        mostrarAlerta("Éxito", "Usuario rechazado/eliminado correctamente.",
                                Alert.AlertType.INFORMATION);
                        cargarUsuarios(); // Recargar tabla
                    } else {
                        mostrarAlerta("Error", "No se pudo rechazar el usuario.", Alert.AlertType.ERROR);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error rechazando usuario: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void configurarBotonesAdmin() {
        btnUsuario.setVisible(true);
        btnFormatoPropuesta.setVisible(true);
        btnEvaluadores.setVisible(true);
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnFormatoPropuestaClicked() {
        if (usuarioActual != null) {
            navigation.showManagementAdmin();
        }
    }

    @FXML
    private void onBtnEvaluadoresClicked() {
        if (usuarioActual != null) {
            navigation.showManagementEvaluadores();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void handleBackToHome() {
        if (usuarioActual != null) {
            navigation.showHomeAdmin();
        }
    }

    @FXML
    private void onBtnActualizarClicked() {
        cargarUsuarios();
        mostrarAlerta("Actualizado", "Lista de usuarios actualizada", Alert.AlertType.INFORMATION);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
    }
}