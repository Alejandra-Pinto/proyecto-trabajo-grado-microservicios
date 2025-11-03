package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PublishedTeacherFormatAController {

    @FXML private AnchorPane mainAnchorPane;
    @FXML private TableView<DegreeWork> tblEstadosFormato;
    @FXML private TableColumn<DegreeWork, String> colNumeroFormato;
    @FXML private TableColumn<DegreeWork, String> colEmailEstudiante;
    @FXML private TableColumn<DegreeWork, String> colFechaActual;
    @FXML private TableColumn<DegreeWork, String> colEstado;
    @FXML private TableColumn<DegreeWork, Void> colAcciones;
    @FXML private ComboBox<String> comboClasificar;
    @FXML private ToggleButton btnRol;
    @FXML private ToggleButton btnFormatoDocente;
    @FXML private ToggleButton btnAnteproyectoDocente;
    @FXML private Button btnAgregarPropuesta;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<DegreeWork> todosLosFormatos;

    public PublishedTeacherFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            cargarFormatosDelDocente();
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null && "PROFESSOR".equalsIgnoreCase(usuario.getRole())) {
            configurarBotonesDocente();
            cargarFormatosDelDocente();
        }
    }

    private void configurarInterfaz() {
        //Configurar ComboBox de filtros
        comboClasificar.getItems().addAll(
            "Todos",
            "Aceptado",
            "No aceptado", 
            "Primera evaluación",
            "Segunda evaluación",
            "Tercera evaluación",
            "Rechazado",
            "Fecha más reciente",
            "Fecha más antigua"
        );
        comboClasificar.setValue("Todos");

        comboClasificar.setOnAction(event -> aplicarFiltro(comboClasificar.getValue()));

        //Configurar columnas de la tabla
        configurarColumnasTabla();
    }

    private void configurarColumnasTabla() {
        //Título del proyecto
        colNumeroFormato.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getTituloProyecto() != null ? data.getValue().getTituloProyecto() : ""
        ));

        //Email del estudiante
        colEmailEstudiante.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEstudiante() != null && data.getValue().getEstudiante().getEmail() != null ? 
            data.getValue().getEstudiante().getEmail() : "Sin estudiante"
        ));

        //Fecha
        colFechaActual.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFechaActual() != null ? data.getValue().getFechaActual().toString() : "N/A"
        ));

        //Estado
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEstado() != null ? data.getValue().getEstado().toString() : ""
        ));

        //Columna de acciones (botón "Ver Correcciones")
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final Button btnCorrections = new Button("Ver Correcciones");

                    {
                        btnCorrections.setStyle("-fx-background-color: #111F63; -fx-text-fill: white; -fx-padding: 5;");
                        btnCorrections.setOnAction(event -> {
                            DegreeWork formato = getTableView().getItems().get(getIndex());
                            if (formato != null) {
                                String estado = formato.getEstado() != null ? formato.getEstado().toString() : "";
                                if ("NO_ACEPTADO".equals(estado) || "RECHAZADO".equals(estado)) {
                                    abrirVentanaCorrecciones(formato);
                                } else {
                                    mostrarAlerta("Acción no permitida", 
                                        "Solo se pueden ver correcciones para estados 'No aceptado' o 'Rechazado'.", 
                                        Alert.AlertType.WARNING);
                                }
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
                            String estado = formato.getEstado() != null ? formato.getEstado().toString() : "";
                            if ("NO_ACEPTADO".equals(estado) || "RECHAZADO".equals(estado)) {
                                setGraphic(btnCorrections);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private void cargarFormatosDelDocente() {
        if (usuarioActual == null) {
            return;
        }

        try {
            //Obtener formatos del docente desde microservicio
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "/docente/" + usuarioActual.getEmail(), 
                DegreeWork[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] formatosArray = response.getBody();
                
                //Filtrar para obtener solo el último formato por estudiante
                Map<String, DegreeWork> ultimosPorEstudiante = Arrays.stream(formatosArray)
                    .filter(f -> f.getEstudiante() != null && f.getEstudiante().getEmail() != null)
                    .collect(Collectors.toMap(
                        f -> f.getEstudiante().getEmail(),
                        f -> f,
                        (f1, f2) -> f1.getId() > f2.getId() ? f1 : f2
                    ));

                List<DegreeWork> ultimosFormatos = new ArrayList<>(ultimosPorEstudiante.values());
                todosLosFormatos = FXCollections.observableArrayList(ultimosFormatos);
                
                //Aplicar filtro inicial
                aplicarFiltro("Todos");
                
                System.out.println("Formatos del docente cargados: " + ultimosFormatos.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando formatos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void aplicarFiltro(String opcion) {
        if (opcion == null || todosLosFormatos == null) {
            return;
        }

        List<DegreeWork> base = new ArrayList<>(todosLosFormatos);

        switch (opcion) {
            case "Todos":
                tblEstadosFormato.getItems().setAll(base);
                break;

            case "Aceptado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "ACEPTADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "NO_ACEPTADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Primera evaluación":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "PRIMERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda evaluación":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "SEGUNDA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera evaluación":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "TERCERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Rechazado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "RECHAZADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            if (f1.getFechaActual() == null && f2.getFechaActual() == null) return 0;
                            if (f1.getFechaActual() == null) return 1;
                            if (f2.getFechaActual() == null) return -1;
                            return f2.getFechaActual().compareTo(f1.getFechaActual());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más antigua":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            if (f1.getFechaActual() == null && f2.getFechaActual() == null) return 0;
                            if (f1.getFechaActual() == null) return 1;
                            if (f2.getFechaActual() == null) return -1;
                            return f1.getFechaActual().compareTo(f2.getFechaActual());
                        })
                        .collect(Collectors.toList())
                );
                break;

            default:
                tblEstadosFormato.getItems().setAll(base);
                break;
        }
    }

    private void abrirVentanaCorrecciones(DegreeWork formato) {
        if (formato != null) {
            //Navegar a la vista de correcciones del docente
            navigation.showTeacherReviewFormatA(usuarioActual, formato);
        }
    }

    private void configurarBotonesDocente() {
        btnRol.setVisible(true);
        btnFormatoDocente.setVisible(true);
        btnAnteproyectoDocente.setVisible(true);
    }

    @FXML
    private void onBtnRolClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation();
        }
    }

    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            cargarFormatosDelDocente();
        }
    }

    @FXML
    private void onBtnAnteproyectoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherFormatA();
        }
    }

    @FXML
    private void onAgregarPropuesta() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherFormatAWithFormato(usuarioActual, null);
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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
