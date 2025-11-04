package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ManagementCoordinatorFormatAController {

    @FXML private TableView<DegreeWork> tableFormatos;
    @FXML private TableColumn<DegreeWork, String> colTitulo;
    @FXML private TableColumn<DegreeWork, String> colEstudiante;
    @FXML private TableColumn<DegreeWork, String> colModalidad;
    @FXML private TableColumn<DegreeWork, String> colFecha;
    @FXML private TableColumn<DegreeWork, String> colEstado;    
    @FXML private TableColumn<DegreeWork, Void> colAccion;
    @FXML private ComboBox<String> comboClasificar;
    @FXML private Button btnClasificar;
    @FXML private ToggleButton btnUsuario;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private List<DegreeWork> todosLosFormatos;

    public ManagementCoordinatorFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarColumnas();
        cargarFormatos();
        inicializarComboBox();
    }

    private void configurarColumnas() {
        //Título
        colTitulo.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getTituloProyecto() != null ? data.getValue().getTituloProyecto() : ""
        ));

        //Estudiante
        colEstudiante.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getEstudiante() != null && data.getValue().getEstudiante().getEmail() != null ? 
            data.getValue().getEstudiante().getEmail() : ""
        ));

        //Modalidad
        colModalidad.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getModalidad() != null ? data.getValue().getModalidad().toString() : ""
        ));

        //Fecha
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getFechaActual() != null ? data.getValue().getFechaActual().toString() : ""
        ));

        //Estado
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getEstado() != null ? data.getValue().getEstado().toString() : ""
        ));

        //Columna con botón "Revisar"
        colAccion.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final Button btn = new Button("Revisar");

                    {
                        btn.setOnAction(event -> {
                            DegreeWork seleccionado = getTableView().getItems().get(getIndex());
                            if (seleccionado != null) {
                                abrirVentanaRevision(seleccionado);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
    }

    private void cargarFormatos() {
        try {
            //Obtener TODOS los formatos desde el microservicio
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "", 
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

                todosLosFormatos = new ArrayList<>(ultimosPorEstudiante.values());
                
                //Aplicar lógica de estados (si es necesario)
                aplicarLogicaEstados();
                
                tableFormatos.getItems().setAll(todosLosFormatos);
                System.out.println("Formatos cargados: " + todosLosFormatos.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando formatos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void aplicarLogicaEstados() {
        //Si necesitas aplicar alguna lógica de estados específica
        // Por ejemplo, actualizar estados basado en reglas de negocio
        for (DegreeWork formato : todosLosFormatos) {
            // Aquí puedes agregar lógica personalizada si es necesario
            // Por ejemplo: if (condición) { formato.setEstado(nuevoEstado); }
        }
    }

    private void abrirVentanaRevision(DegreeWork formato) {
        if (formato != null) {
            //Navegar a la vista de revisión del coordinador
            navigation.showCoordinatorReviewFormatA(usuarioActual, formato);
        }
    }

    private void inicializarComboBox() {
        comboClasificar.getItems().addAll(
            "Todos",
            "Aceptados",
            "No aceptados", 
            "Primera evaluación",
            "Segunda evaluación",
            "Tercera evaluación",
            "Fecha más reciente",
            "Fecha más antigua"
        );
        comboClasificar.getSelectionModel().selectFirst();

        comboClasificar.setOnAction(event -> {
            aplicarFiltro(comboClasificar.getValue());
        });
    }

    private void aplicarFiltro(String opcion) {
        if (opcion == null || todosLosFormatos == null) {
            return;
        }

        List<DegreeWork> base = new ArrayList<>(todosLosFormatos);

        switch (opcion) {
            case "Todos":
                tableFormatos.getItems().setAll(base);
                break;

            case "Aceptados":
                tableFormatos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "ACEPTADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptados":
                tableFormatos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "RECHAZADO".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Primera evaluación":
                tableFormatos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "PRIMERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda evaluación":
                tableFormatos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "SEGUNDA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera evaluación":
                tableFormatos.getItems().setAll(
                    base.stream()
                        .filter(f -> f.getEstado() != null && "TERCERA_EVALUACION".equalsIgnoreCase(f.getEstado().toString()))
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tableFormatos.getItems().setAll(
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
                tableFormatos.getItems().setAll(
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
                tableFormatos.getItems().setAll(base);
                break;
        }
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
        cargarFormatos();
        mostrarAlerta("Actualizado", "Lista de formatos actualizada", Alert.AlertType.INFORMATION);
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