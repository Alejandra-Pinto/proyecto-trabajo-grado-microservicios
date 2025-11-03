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
public class PublishedDepartmentHeadDraftController {

    @FXML private AnchorPane mainAnchorPane;
    @FXML private TableView<DegreeWork> tblAnteproyectos;
    @FXML private TableColumn<DegreeWork, String> colTitulo;
    @FXML private TableColumn<DegreeWork, String> colEmailDocente;
    @FXML private TableColumn<DegreeWork, String> colEmailEstudiante;
    @FXML private TableColumn<DegreeWork, String> colFechaActual;
    @FXML private TableColumn<DegreeWork, String> colEstado;
    @FXML private TableColumn<DegreeWork, Void> colAcciones;
    @FXML private ComboBox<String> comboClasificar;
    @FXML private ToggleButton btnRol;
    @FXML private ToggleButton btnEvaluarPropuestas;
    @FXML private ToggleButton btnEvaluarAnteproyectos;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<DegreeWork> todosLosAnteproyectos;

    public PublishedDepartmentHeadDraftController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        
        if (usuarioActual != null && "DEPARTMENT_HEAD".equalsIgnoreCase(usuarioActual.getRole())) {
            cargarAnteproyectos();
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null && "DEPARTMENT_HEAD".equalsIgnoreCase(usuario.getRole())) {
            configurarBotonesJefeDepartamento();
            cargarAnteproyectos();
        }
    }

    private void configurarInterfaz() {
        // Configurar ComboBox de filtros (mismos estados que formato A)
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

        // Configurar columnas de la tabla
        configurarColumnasTabla();
    }

    private void configurarColumnasTabla() {
        // Título del anteproyecto
        colTitulo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getTituloProyecto() != null ? data.getValue().getTituloProyecto() : ""
        ));

        // Email del docente (director)
        colEmailDocente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDirectorProyecto() != null && data.getValue().getDirectorProyecto().getEmail() != null ? 
            data.getValue().getDirectorProyecto().getEmail() : "Sin docente"
        ));

        // Email del estudiante
        colEmailEstudiante.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEstudiante() != null && data.getValue().getEstudiante().getEmail() != null ? 
            data.getValue().getEstudiante().getEmail() : "Sin estudiante"
        ));

        // Fecha
        colFechaActual.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFechaActual() != null ? data.getValue().getFechaActual().toString() : "N/A"
        ));

        // Estado (usa el estado del DegreeWork, igual que formato A)
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEstado() != null ? data.getValue().getEstado().toString() : ""
        ));

        // Columna de acciones (botón "Evaluar")
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final Button btnEvaluar = new Button("Evaluar");

                    {
                        btnEvaluar.setStyle("-fx-background-color: #111F63; -fx-text-fill: white; -fx-padding: 5;");
                        btnEvaluar.setOnAction(event -> {
                            DegreeWork anteproyecto = getTableView().getItems().get(getIndex());
                            if (anteproyecto != null) {
                                evaluarAnteproyecto(anteproyecto);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            DegreeWork anteproyecto = getTableRow().getItem();
                            String estado = anteproyecto.getEstado() != null ? anteproyecto.getEstado().toString() : "";
                            // Mostrar botón solo si el anteproyecto está en estado revisable
                            if (esEstadoRevisable(estado)) {
                                setGraphic(btnEvaluar);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private boolean esEstadoRevisable(String estado) {
        return "PRIMERA_EVALUACION".equals(estado) || 
               "SEGUNDA_EVALUACION".equals(estado) || 
               "TERCERA_EVALUACION".equals(estado) ||
               "NO_ACEPTADO".equals(estado);
    }

    private void cargarAnteproyectos() {
        if (usuarioActual == null) {
            return;
        }

        try {
            // Obtener todos los anteproyectos desde microservicio
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "", 
                DegreeWork[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] todosLosProyectos = response.getBody();
                
                // Filtrar solo proyectos que tienen anteproyectos
                List<DegreeWork> proyectosConAnteproyectos = Arrays.stream(todosLosProyectos)
                    .filter(proyecto -> proyecto.getAnteproyectos() != null && 
                                       !proyecto.getAnteproyectos().isEmpty())
                    .collect(Collectors.toList());
                
                todosLosAnteproyectos = FXCollections.observableArrayList(proyectosConAnteproyectos);
                
                // Aplicar filtro inicial
                aplicarFiltro("Todos");
                
                System.out.println("Anteproyectos cargados: " + proyectosConAnteproyectos.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando anteproyectos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void aplicarFiltro(String opcion) {
        if (opcion == null || todosLosAnteproyectos == null) {
            return;
        }

        List<DegreeWork> base = new ArrayList<>(todosLosAnteproyectos);

        switch (opcion) {
            case "Todos":
                tblAnteproyectos.getItems().setAll(base);
                break;

            case "Aceptado":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "ACEPTADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptado":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "NO_ACEPTADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Primera evaluación":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "PRIMERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda evaluación":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "SEGUNDA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera evaluación":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "TERCERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Rechazado":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "RECHAZADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tblAnteproyectos.getItems().setAll(
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
                tblAnteproyectos.getItems().setAll(
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
                tblAnteproyectos.getItems().setAll(base);
                break;
        }
    }

    private void evaluarAnteproyecto(DegreeWork anteproyecto) {
        if (anteproyecto != null) {
            // Navegar a la vista de evaluación del anteproyecto
            // Aquí puedes implementar la navegación a la vista específica de evaluación
            mostrarAlerta("Evaluación", 
                "Evaluando anteproyecto: " + anteproyecto.getTituloProyecto(), 
                Alert.AlertType.INFORMATION);
            
            // TODO: Implementar navegación a vista de evaluación específica
            // navigation.showEvaluationDraft(usuarioActual, anteproyecto);
        }
    }

    private void configurarBotonesJefeDepartamento() {
        btnRol.setVisible(true);
        btnEvaluarPropuestas.setVisible(true);
        btnEvaluarAnteproyectos.setVisible(true);
        
        // Deshabilitar el botón actual (ya estamos en esta vista)
        btnEvaluarAnteproyectos.setDisable(true);
        btnEvaluarAnteproyectos.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
    }

    @FXML
    private void onBtnRolClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnEvaluarPropuestasClicked() {
        if (usuarioActual != null && "DEPARTMENT_HEAD".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementCoordinatorFormatA();
        }
    }

    @FXML
    private void onBtnEvaluarAnteproyectosClicked() {
        // Ya estamos en esta vista, no hacer nada
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}