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

    @FXML private TableView<User> tblCoordinadores;
    @FXML private TableColumn<User, String> colCorreo;
    @FXML private TableColumn<User, String> colNombre;
    @FXML private TableColumn<User, String> colEstado;
    @FXML private TableColumn<User, Void> colAcciones;
    @FXML private Button btnGuardarCambios;
    @FXML private ToggleButton btnUsuario;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<User> coordinadores;

    public ManagementAdminController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarTabla();
        cargarCoordinadores();
    }

    private void configurarTabla() {
        //Columna Correo
        colCorreo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEmail() != null ? data.getValue().getEmail() : ""
        ));

        //Columna Nombre
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            (data.getValue().getFirstName() != null ? data.getValue().getFirstName() : "") + " " +
            (data.getValue().getLastName() != null ? data.getValue().getLastName() : "")
        ));

        //Columna Estado con ComboBox
        colEstado.setCellFactory(new Callback<TableColumn<User, String>, TableCell<User, String>>() {
            @Override
            public TableCell<User, String> call(TableColumn<User, String> param) {
                return new TableCell<User, String>() {
                    private final ComboBox<String> combo = new ComboBox<>();

                    {
                        combo.getItems().addAll("PENDIENTE", "ACEPTADO", "RECHAZADO");
                        combo.setOnAction(e -> {
                            User coordinador = getTableView().getItems().get(getIndex());
                            if (coordinador != null) {
                                coordinador.setStatus(combo.getValue());
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String estado, boolean empty) {
                        super.updateItem(estado, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            User coordinador = getTableRow().getItem();
                            combo.setValue(coordinador.getStatus() != null ? coordinador.getStatus() : "PENDIENTE");
                            setGraphic(combo);
                        }
                    }
                };
            }
        });

        //Columna de acciones
        colAcciones.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button btnEliminar = new Button("Eliminar");

                    {
                        btnEliminar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        btnEliminar.setOnAction(event -> {
                            User coordinador = getTableView().getItems().get(getIndex());
                            if (coordinador != null) {
                                eliminarCoordinador(coordinador);
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

    private void cargarCoordinadores() {
        try {
            //Obtener coordinadores desde microservicio
            ResponseEntity<User[]> response = apiService.get(
                "api/usuarios", 
                "/rol/COORDINATOR", 
                User[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<User> coordinadoresList = Arrays.asList(response.getBody());
                coordinadores = FXCollections.observableArrayList(coordinadoresList);
                tblCoordinadores.setItems(coordinadores);
                System.out.println("Coordinadores cargados: " + coordinadoresList.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando coordinadores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onGuardarCambios() {
        if (coordinadores == null || coordinadores.isEmpty()) {
            mostrarAlerta("Información", "No hay coordinadores para actualizar.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            boolean huboCambios = false;
            int cambiosExitosos = 0;

            for (User coordinador : coordinadores) {
                //Actualizar cada coordinador en el microservicio
                ResponseEntity<User> response = apiService.put(
                    "api/usuarios", 
                    "/" + coordinador.getEmail() + "/estado", 
                    coordinador, 
                    User.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    cambiosExitosos++;
                    huboCambios = true;
                }
            }

            if (huboCambios) {
                mostrarAlerta("Éxito", 
                    cambiosExitosos + " coordinador(es) actualizado(s) correctamente.", 
                    Alert.AlertType.INFORMATION);
                //Recargar datos para verificar cambios
                cargarCoordinadores();
            } else {
                mostrarAlerta("Aviso", "No se realizaron cambios en los estados.", Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error actualizando coordinadores: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarCoordinador(User coordinador) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar al coordinador?");
        confirmacion.setContentText("Coordinador: " + coordinador.getEmail());

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    //Eliminar coordinador del microservicio
                    ResponseEntity<Void> deleteResponse = apiService.delete(
                        "api/usuarios", 
                        "/" + coordinador.getEmail()
                    );

                    if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                        mostrarAlerta("Éxito", "Coordinador eliminado correctamente.", Alert.AlertType.INFORMATION);
                        cargarCoordinadores(); // Recargar tabla
                    } else {
                        mostrarAlerta("Error", "No se pudo eliminar el coordinador.", Alert.AlertType.ERROR);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Error", "Error eliminando coordinador: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation();
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
        cargarCoordinadores();
        mostrarAlerta("Actualizado", "Lista de coordinadores actualizada", Alert.AlertType.INFORMATION);
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