package com.unicauca.front.controller;

import com.unicauca.front.dto.ActualizarEvaluacionDTO;
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

        // Columna Estado - SOLO LECTURA (sin ComboBox editable)
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            obtenerEstadoDisplay(data.getValue())
        ));

        // Columna de acciones
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final Button btnRevisar = new Button("Revisar");
                    private final Button btnDetalles = new Button("Detalles");

                    {
                        // Botón Revisar - va a la pantalla de evaluación detallada
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
                            DegreeWork formato = getTableRow().getItem();
                            
                            // Solo mostrar botón Revisar si el formato está pendiente o necesita revisión
                            boolean mostrarRevisar = esFormatoRevisable(formato);
                            
                            // Crear contenedor para los botones
                            javafx.scene.layout.HBox botones = new javafx.scene.layout.HBox(5);
                            if (mostrarRevisar) {
                                botones.getChildren().add(btnRevisar);
                            }
                            botones.getChildren().add(btnDetalles);
                            setGraphic(botones);
                        }
                    }
                };
            }
        });
    }

    // Método para determinar si un formato es revisable
    private boolean esFormatoRevisable(DegreeWork formato) {
        if (formato == null || formato.getFormatosA() == null || formato.getFormatosA().isEmpty()) {
            return false;
        }
        
        Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
        if (ultimoFormatoA.getEstado() == null) {
            return true; // Si no tiene estado, se puede revisar
        }
        
        String estado = ultimoFormatoA.getEstado().toString();
        // Se puede revisar si está pendiente, en corrección, o no aceptado
        return "PENDIENTE".equalsIgnoreCase(estado) || 
           "NO_ACEPTADO".equalsIgnoreCase(estado) ||
           "EN_CORRECCION".equalsIgnoreCase(estado) ||
           "PRIMERA_REVISION".equalsIgnoreCase(estado) ||
           "SEGUNDA_REVISION".equalsIgnoreCase(estado) ||
           "TERCERA_REVISION".equalsIgnoreCase(estado) ||
           "EN_REVISION".equalsIgnoreCase(estado);
    }

    // Método para obtener el estado display del formato
    private String obtenerEstadoDisplay(DegreeWork formato) {
        if (formato == null || formato.getFormatosA() == null || formato.getFormatosA().isEmpty()) {
            return "SIN DOCUMENTO";
        }
        
        Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
        if (ultimoFormatoA.getEstado() == null) {
            return "PENDIENTE";
        }
        
        String estado = ultimoFormatoA.getEstado().toString();
        
        // Traducir estados a formato más amigable
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return "Pendiente";
            case "EN_REVISION":
                return "En revisión";
            case "ACEPTADO":
                return "Aceptado";
            case "NO_ACEPTADO":
                return "No aceptado";
            case "EN_CORRECCION":
                return "En corrección";
            case "RECHAZADO":
                return "Rechazado";
            case "PRIMERA_REVISION":
                return "Primera revisión";
            case "SEGUNDA_REVISION":
                return "Segunda revisión";
            case "TERCERA_REVISION":
                return "Tercera revisión";
            default:
                return estado;
        }
    }

    private void configurarFiltros() {
        comboFiltro.getItems().addAll(
            "Todos los formatos",
            "Pendientes",
            "Aceptados", 
            "Rechazados",
            "No aceptados",
            "En revisión",
            "En corrección"
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
                    tieneEstado(f, "PENDIENTE")));
                break;
            case "Aceptados":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    tieneEstado(f, "ACEPTADO")));
                break;
            case "Rechazados":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    tieneEstado(f, "RECHAZADO")));
                break;
            case "No aceptados":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    tieneEstado(f, "NO_ACEPTADO")));
                break;
            case "En revisión":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    tieneEstado(f, "EN_REVISION") || 
                    tieneEstado(f, "PRIMERA_REVISION") ||
                    tieneEstado(f, "SEGUNDA_REVISION") ||
                    tieneEstado(f, "TERCERA_REVISION")));
                break;
            case "En corrección":
                formatosFiltrados.addAll(formatos.filtered(f -> 
                    tieneEstado(f, "EN_CORRECCION")));
                break;
            default:
                formatosFiltrados.addAll(formatos);
        }

        tblFormatos.setItems(formatosFiltrados);
    }

    private boolean tieneEstado(DegreeWork formato, String estadoBuscado) {
        if (formato.getFormatosA() == null || formato.getFormatosA().isEmpty()) {
            return false;
        }
        
        Document ultimoFormatoA = formato.getFormatosA().get(formato.getFormatosA().size() - 1);
        if (ultimoFormatoA.getEstado() == null) {
            return "PENDIENTE".equalsIgnoreCase(estadoBuscado);
        }
        
        return ultimoFormatoA.getEstado().toString().equalsIgnoreCase(estadoBuscado);
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
        // Este botón ya no es necesario porque no se editan estados directamente en la tabla
        mostrarAlerta("Información", 
            "Para evaluar formatos, haz clic en 'Revisar' en la columna de acciones.", 
            Alert.AlertType.INFORMATION);
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
        
        String estado = obtenerEstadoDisplay(formato);
        
        String contenido = String.format(
            "ID: %s\n" +
            "Título: %s\n" +
            "Estudiante: %s\n" +
            "Modalidad: %s\n" +
            "Estado: %s\n" +
            "Fecha: %s\n" +
            "Director: %s\n" +
            "Codirector: %s",
            formato.getId() != null ? formato.getId().toString() : "N/A",
            formato.getTituloProyecto() != null ? formato.getTituloProyecto() : "N/A",
            obtenerNombreEstudiante(formato),
            obtenerModalidadDisplay(formato.getModalidad()),
            estado,
            formato.getFechaActual() != null ? formato.getFechaActual().toString() : "N/A",
            formato.getDirectorProyecto() != null ? formato.getDirectorProyecto().getEmail() : "N/A",
            formato.getCodirectorProyecto() != null ? formato.getCodirectorProyecto().getEmail() : "N/A"
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