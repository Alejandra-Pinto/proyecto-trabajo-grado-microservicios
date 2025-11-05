package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.EnumEstadoDegreeWork;
import com.unicauca.front.model.EnumEstadoDocument;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Documented;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ManagementCoordinatorFormatAController {

    @FXML private TableView<DegreeWork> tblFormatos;
    @FXML private TableColumn<DegreeWork, String> colTitulo;
    @FXML private TableColumn<DegreeWork, String> colEstudiante;
    @FXML private TableColumn<DegreeWork, String> colModalidad;
    @FXML private TableColumn<DegreeWork, String> colFecha;
    @FXML private TableColumn<DegreeWork, String> colEstado;
    @FXML private TableColumn<DegreeWork, Void> colAcciones;
    @FXML private Button btnGuardarCambios;
    @FXML private ToggleButton btnUsuario;
    @FXML private ComboBox<String> comboFiltro;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<DegreeWork> formatos;

    public ManagementCoordinatorFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarTabla();
        configurarFiltros();
        cargarFormatos(true);
    }

private void configurarTabla() {
    // Columna Título
    colTitulo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getTituloProyecto() != null ? data.getValue().getTituloProyecto() : "Sin título"
    ));

    // Columna Estudiante
    colEstudiante.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        obtenerNombreEstudiante(data.getValue())
    ));

    // Columna Modalidad
    colModalidad.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        obtenerModalidadDisplay(data.getValue().getModalidad())
    ));

    // Columna Fecha
    colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getFechaActual() != null ? 
        data.getValue().getFechaActual().toString() : "Sin fecha"
    ));

    // Columna Estado con ComboBox - CORREGIDO
    colEstado.setCellFactory(new Callback<TableColumn<DegreeWork, String>, TableCell<DegreeWork, String>>() {
        @Override
        public TableCell<DegreeWork, String> call(TableColumn<DegreeWork, String> param) {
            return new TableCell<DegreeWork, String>() {
                private final ComboBox<String> combo = new ComboBox<>();

                {
                    combo.getItems().addAll("NO_ACEPTADO", "ACEPTADO", "RECHAZADO", 
                                           "PRIMERA_REVISION", "SEGUNDA_REVISION", "TERCERA_REVISION");
                    combo.setOnAction(e -> {
                        DegreeWork formato = getTableView().getItems().get(getIndex());
                        if (formato != null) {
                            // CORRECCIÓN: Convertir String a Enum antes de asignar
                            String estadoSeleccionado = combo.getValue();
                            EnumEstadoDocument estadoEnum = convertirStringAEnumEstadoD(estadoSeleccionado);
                            if (estadoEnum != null) {
                                Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
                                ultimoFormatoA.setEstado(estadoEnum);
                                actualizarEstadoFormato(formato, estadoEnum);
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(String estado, boolean empty) {
                    super.updateItem(estado, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        DegreeWork formato = getTableRow().getItem();

                        String estadoActual = "PENDIENTE";
                        if (formato.getFormatosA() != null && !formato.getFormatosA().isEmpty()) {
                            Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
                            if (ultimoFormatoA.getEstado() != null) {
                                estadoActual = ultimoFormatoA.getEstado().toString();
                            }
                        }

                        combo.setValue(estadoActual);
                        setGraphic(combo);
                    }
                }

            };
        }
    });

    // Columna de acciones (se mantiene igual)
    colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
        @Override
        public TableCell<DegreeWork, Void> call(TableColumn<DegreeWork, Void> param) {
            return new TableCell<DegreeWork, Void>() {
                private final Button btnRevisar = new Button("Revisar");
                private final Button btnDetalles = new Button("Detalles");

                {
                    // Botón Revisar
                    btnRevisar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    btnRevisar.setOnAction(event -> {
                        DegreeWork formato = getTableView().getItems().get(getIndex());
                        if (formato != null) {
                            revisarFormato(formato);
                        }
                    });

                    // Botón Detalles
                    btnDetalles.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                    btnDetalles.setOnAction(event -> {
                        DegreeWork formato = getTableView().getItems().get(getIndex());
                        if (formato != null) {
                            verDetallesFormato(formato);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        // Crear contenedor para los botones
                        javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(5);
                        botones.getChildren().addAll(btnRevisar, btnDetalles);
                        setGraphic(botones);
                    }
                }
            };
        }
    });
}

// AGREGAR ESTOS MÉTODOS AUXILIARES A LA CLASE:

/**
 * Convierte un String al EnumEstadoDegreeWork correspondiente
 */
private EnumEstadoDegreeWork convertirStringAEnumEstado(String estadoStr) {
    if (estadoStr == null) return null;
    
    try {
        return EnumEstadoDegreeWork.valueOf(estadoStr);
    } catch (IllegalArgumentException e) {
        System.out.println("Error: Estado no válido - " + estadoStr);
        mostrarAlerta("Error", "Estado no válido: " + estadoStr, Alert.AlertType.ERROR);
        return null;
    }
}
private EnumEstadoDocument convertirStringAEnumEstadoD(String estadoStr) {
    if (estadoStr == null) return null;
    
    try {
        return EnumEstadoDocument.valueOf(estadoStr);
    } catch (IllegalArgumentException e) {
        System.out.println("Error: Estado no válido - " + estadoStr);
        Platform.runLater(() -> {
            mostrarAlerta("Error", "Estado no válido: " + estadoStr, Alert.AlertType.ERROR);
        });
        return null;
    }
}

/**
 * Actualiza el estado del último Formato A (Document) dentro del DegreeWork
 * y envía la actualización al microservicio de degreeworks.
 */
private void actualizarEstadoFormato(DegreeWork formato, EnumEstadoDocument nuevoEstado) {
    try {
        if (formato.getFormatosA() == null || formato.getFormatosA().isEmpty()) {
            mostrarAlerta("Error", "Este trabajo de grado no tiene formatos A registrados.", Alert.AlertType.ERROR);
            return;
        }

        // ✅ Tomar el último formato A
        Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
        ultimoFormatoA.setEstado(nuevoEstado);

        System.out.println("🟦 Actualizando estado del último Formato A:");
        System.out.println(" - DegreeWork ID: " + formato.getId());
        System.out.println(" - Documento ID: " + ultimoFormatoA.getId());
        System.out.println(" - Nuevo estado: " + nuevoEstado);

        // ✅ Enviar el DegreeWork completo al backend
        ResponseEntity<DegreeWork> response = apiService.put(
            "api/degreeworks",
            "/" + formato.getId(),
            formato,  // enviamos el objeto completo
            DegreeWork.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("✅ Estado del Formato A actualizado correctamente en DegreeWork ID: " + formato.getId());
            Platform.runLater(() -> {
                mostrarAlerta("Éxito", "Estado del Formato A actualizado correctamente a " + nuevoEstado, Alert.AlertType.INFORMATION);
                tblFormatos.refresh();
            });
        } else {
            System.out.println("❌ Error al actualizar Formato A: " + response.getStatusCode());
            mostrarAlerta("Error", "No se pudo actualizar el Formato A en el trabajo de grado.", Alert.AlertType.ERROR);
        }

    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Error", "Error actualizando el estado del Formato A: " + e.getMessage(), Alert.AlertType.ERROR);
    }
}

    private void configurarFiltros() {
        comboFiltro.getItems().addAll(
            "Todos los formatos",
            "Pendientes",
            "Aceptados", 
            "Rechazados",
            "En primera evaluación",
            "En segunda evaluación",
            "En tercera evaluación"
        );
        comboFiltro.getSelectionModel().selectFirst();

        comboFiltro.setOnAction(event -> {
            aplicarFiltro(comboFiltro.getValue());
        });
    }

    private void aplicarFiltro(String filtro) {
        if (formatos == null || filtro == null) return;

        ObservableList<DegreeWork> formatosFiltrados = FXCollections.observableArrayList();

        switch (filtro) {
            case "Todos los formatos":
                formatosFiltrados.addAll(formatos);
                break;
            case "Pendientes":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "PENDIENTE".equalsIgnoreCase(f.getEstado().toString())));
                break;
            case "Aceptados":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "ACEPTADO".equalsIgnoreCase(f.getEstado().toString())));
                break;
            case "Rechazados":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "RECHAZADO".equalsIgnoreCase(f.getEstado().toString())));
                break;
            case "En primera evaluación":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "PRIMERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString())));
                break;
            case "En segunda evaluación":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "SEGUNDA_EVALUACION".equalsIgnoreCase(f.getEstado().toString())));
                break;
            case "En tercera evaluación":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    "TERCERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString())));
                break;
            default:
                formatosFiltrados.addAll(formatos);
        }

        tblFormatos.setItems(formatosFiltrados);
    }

    private String obtenerNombreEstudiante(DegreeWork formato) {
        if (formato.getEstudiante() == null) {
            return "Sin estudiante asignado";
        }
        
        String nombre = formato.getEstudiante().getFirstName() != null ? 
                       formato.getEstudiante().getFirstName() : "";
        String apellido = formato.getEstudiante().getLastName() != null ? 
                         formato.getEstudiante().getLastName() : "";
        
        if (nombre.isEmpty() && apellido.isEmpty()) {
            return formato.getEstudiante().getEmail() != null ? 
                   formato.getEstudiante().getEmail() : "Estudiante sin información";
        }
        
        return nombre + " " + apellido;
    }

    private String obtenerModalidadDisplay(Object modalidad) {
        if (modalidad == null) return "No definida";
        
        String modalidadStr = modalidad.toString();
        switch (modalidadStr.toUpperCase()) {
            case "INVESTIGACION":
                return "Investigación";
            case "PRACTICA_PROFESIONAL":
                return "Práctica Profesional";
            default:
                return modalidadStr;
        }
    }

    private void cargarFormatos(boolean mostrarAlerta) {
        try {
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "", 
                DegreeWork[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] formatosArray = response.getBody();
                formatos = FXCollections.observableArrayList(Arrays.asList(formatosArray));
                tblFormatos.setItems(formatos);
                
                System.out.println("Formatos cargados: " + formatos.size());
                if (mostrarAlerta) {
                    mostrarAlerta("Éxito", "Se cargaron " + formatos.size() + " formatos", Alert.AlertType.INFORMATION);
                }
            } else if (mostrarAlerta) {
                mostrarAlerta("Error", "No se pudieron cargar los formatos", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (mostrarAlerta) {
                mostrarAlerta("Error", "Error cargando formatos: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    @FXML
    private void onGuardarCambios() {
        if (formatos == null || formatos.isEmpty()) {
            mostrarAlerta("Información", "No hay formatos para actualizar.", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            boolean huboCambios = false;
            int cambiosExitosos = 0;

            for (DegreeWork formato : formatos) {
                // Verificar si el estado cambió (necesitarías trackear el estado original)
                // Por ahora, actualizamos todos los formatos
                
                ResponseEntity<DegreeWork> response = apiService.put(
                    "api/degreeworks", 
                    "/" + formato.getId(), 
                    convertirADTO(formato),
                    DegreeWork.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    cambiosExitosos++;
                    huboCambios = true;
                    System.out.println("Formato actualizado: " + formato.getId());
                }
            }

            if (huboCambios) {
                mostrarAlerta("Éxito", 
                    cambiosExitosos + " formato(s) actualizado(s) correctamente.", 
                    Alert.AlertType.INFORMATION);
                cargarFormatos(false); // Recargar para ver cambios
            } else {
                mostrarAlerta("Aviso", "No se realizaron cambios en los formatos.", Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error actualizando formatos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Object convertirADTO(DegreeWork formato) {
        // Necesitas crear un DTO similar al que usa el backend
        // Por ahora, devolvemos el objeto mismo (ajusta según tu API)
        return formato;
    }

    private void revisarFormato(DegreeWork formato) {
        try {
            if (formato != null) {
                System.out.println("Revisando formato ID: " + formato.getId());
                // Navegar a la pantalla de revisión detallada
                navigation.showCoordinatorReviewFormatA(usuarioActual, formato);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la revisión: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

private void verDetallesFormato(DegreeWork formato) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Detalles del Formato");
    alert.setHeaderText("Información del Trabajo de Grado");
    
    String contenido = String.format(
        "ID: %s\n" +
        "Título: %s\n" +
        "Estudiante: %s\n" +
        "Modalidad: %s\n" +
        "Estado: %s\n" +
        "Fecha: %s",
        formato.getId() != null ? formato.getId().toString() : "N/A",
        formato.getTituloProyecto() != null ? formato.getTituloProyecto() : "N/A",
        obtenerNombreEstudiante(formato),
        obtenerModalidadDisplay(formato.getModalidad()),
        formato.getEstado() != null ? formato.getEstado().toString() : "N/A",
        formato.getFechaActual() != null ? formato.getFechaActual().toString() : "N/A"
    );
    
    alert.setContentText(contenido);
    alert.showAndWait();
}

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
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
            navigation.showHomeWithUser(usuarioActual);
        }
    }

    @FXML
    private void onBtnActualizarClicked() {
        cargarFormatos(false);
        mostrarAlerta("Actualizado", "Lista de formatos actualizada", Alert.AlertType.INFORMATION);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }


    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
    }
}