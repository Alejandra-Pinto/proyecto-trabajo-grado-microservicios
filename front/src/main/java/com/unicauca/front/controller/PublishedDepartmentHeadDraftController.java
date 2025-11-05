package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
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
        // Configurar ComboBox de filtros - ACTUALIZADO a estados de documento
        comboClasificar.getItems().addAll(
            "Todos",
            "Aceptado",
            "No aceptado", 
            "Primera revisión",
            "Segunda revisión",
            "Tercera revisión",
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
         // Configurar anchos de columnas
        colTitulo.setPrefWidth(200);        // Título más ancho
        colEmailDocente.setPrefWidth(180);  // Email docente
        colEmailEstudiante.setPrefWidth(180); // Email estudiante  
        colFechaActual.setPrefWidth(120);   // Fecha
        colEstado.setPrefWidth(150);        // Estado
        colAcciones.setPrefWidth(300);      // Acciones MÁS ANCHA para los ComboBox

        // Título del anteproyecto - TEMPORAL para debugging
        colTitulo.setCellValueFactory(data -> {
            String titulo = data.getValue().getTituloProyecto();
            if (titulo == null || titulo.isEmpty()) {
                titulo = "ID: " + data.getValue().getId(); // Mostrar ID si no hay título
            }
            return new javafx.beans.property.SimpleStringProperty(titulo);
        });

        // Email del docente (director)
        colEmailDocente.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDirectorProyecto() != null && data.getValue().getDirectorProyecto().getEmail() != null ? 
            data.getValue().getDirectorProyecto().getEmail() : "Sin docente"
        ));

        // Email del estudiante - TEMPORAL para debugging
        colEmailEstudiante.setCellValueFactory(data -> {
            String emailEstudiante = "Sin estudiantes";
            if (data.getValue().getEstudiantes() != null && !data.getValue().getEstudiantes().isEmpty()) {
                User primerEstudiante = data.getValue().getEstudiantes().get(0);
                if (primerEstudiante != null && primerEstudiante.getEmail() != null) {
                    emailEstudiante = primerEstudiante.getEmail();
                }
            }
            return new javafx.beans.property.SimpleStringProperty(emailEstudiante);
        });

        // Fecha - del último anteproyecto o del DegreeWork
        colFechaActual.setCellValueFactory(data -> {
            String fecha = "N/A";
            
            // Primero intentar con el último anteproyecto
            Document ultimoAnteproyecto = obtenerUltimoAnteproyecto(data.getValue());
            if (ultimoAnteproyecto != null && ultimoAnteproyecto.getFechaActual() != null) {
                fecha = ultimoAnteproyecto.getFechaActual().toString();
            } 
            // Si no hay anteproyecto, usar la fecha del DegreeWork
            else if (data.getValue().getFechaActual() != null) {
                fecha = data.getValue().getFechaActual().toString();
            }
            
            return new javafx.beans.property.SimpleStringProperty(fecha);
        });

        // Estado - del último anteproyecto
        // Estado - Si no hay anteproyecto, mostrar el estado del DegreeWork
        colEstado.setCellValueFactory(data -> {
            Document ultimoAnteproyecto = obtenerUltimoAnteproyecto(data.getValue());
            String estado = "Sin anteproyecto";
            
            if (ultimoAnteproyecto != null && ultimoAnteproyecto.getEstado() != null) {
                estado = ultimoAnteproyecto.getEstado().toString();
            } else if (data.getValue().getEstado() != null) {
                // Si no hay documento anteproyecto, mostrar el estado del DegreeWork
                estado = data.getValue().getEstado().toString();
            }
            
            return new javafx.beans.property.SimpleStringProperty(estado);
        });

        // Columna de acciones (botón "Evaluar")
        // Cambia la columna de acciones para incluir ComboBox de evaluadores
        // Cambia la columna de acciones para incluir ComboBox de evaluadores
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final ComboBox<String> comboEvaluador1 = new ComboBox<>();
                    private final ComboBox<String> comboEvaluador2 = new ComboBox<>();
                    private final HBox hbox = new HBox(5);
                    private final Label lblAsignar = new Label("Asignar:");
                    
                    {
                        // Configurar ComboBox más compactos
                        comboEvaluador1.setPromptText("Eval1");
                        comboEvaluador2.setPromptText("Eval2");
                        comboEvaluador1.setPrefWidth(120);
                        comboEvaluador2.setPrefWidth(120);
                        comboEvaluador1.setMaxWidth(120);
                        comboEvaluador2.setMaxWidth(120);
                        
                        // Estilo más compacto
                        lblAsignar.setStyle("-fx-font-size: 10px; -fx-padding: 0 5 0 0;");
                        hbox.setStyle("-fx-alignment: center-left; -fx-padding: 2;");
                        
                        hbox.getChildren().addAll(lblAsignar, comboEvaluador1, comboEvaluador2);
                        
                        // Cargar evaluadores
                        itemProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal == null) {
                                comboEvaluador1.getItems().clear();
                                comboEvaluador2.getItems().clear();
                            } else {
                                cargarEvaluadores();
                            }
                        });
                    }
                    
                    private void cargarEvaluadores() {
                        try {
                            ResponseEntity<User[]> response = apiService.get(
                                "api/admin", 
                                "/evaluators", 
                                User[].class
                            );
                            
                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                User[] evaluadores = response.getBody();
                                List<String> nombres = Arrays.stream(evaluadores)
                                    .map(user -> user.getFirstName().charAt(0) + ". " + user.getLastName())
                                    .collect(Collectors.toList());
                                
                                ObservableList<String> evaluadoresList = FXCollections.observableArrayList(nombres);
                                comboEvaluador1.setItems(evaluadoresList);
                                comboEvaluador2.setItems(evaluadoresList);
                            }
                        } catch (Exception e) {
                            System.out.println("Error cargando evaluadores: " + e.getMessage());
                            comboEvaluador1.setItems(FXCollections.observableArrayList("Error"));
                            comboEvaluador2.setItems(FXCollections.observableArrayList("Error"));
                        }
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            DegreeWork anteproyecto = getTableRow().getItem();
                            // MOSTRAR SIEMPRE los ComboBox (o ajusta según tu lógica de estados)
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    // Método helper para obtener el último anteproyecto
    private Document obtenerUltimoAnteproyecto(DegreeWork degreeWork) {
        if (degreeWork == null || degreeWork.getAnteproyectos() == null || degreeWork.getAnteproyectos().isEmpty()) {
            return null;
        }
        List<Document> anteproyectos = degreeWork.getAnteproyectos();
        // Obtener el último anteproyecto (el más reciente)
        return anteproyectos.get(anteproyectos.size() - 1);
    }

    // Método helper para obtener fecha para ordenamiento
    private LocalDate obtenerFechaParaOrdenamiento(DegreeWork degreeWork) {
        if (degreeWork == null) return null;
        
        // Primero intentar con el último anteproyecto
        Document ultimoAnteproyecto = obtenerUltimoAnteproyecto(degreeWork);
        if (ultimoAnteproyecto != null && ultimoAnteproyecto.getFechaActual() != null) {
            return ultimoAnteproyecto.getFechaActual();
        }
        
        // Si no hay anteproyecto, usar la fecha del DegreeWork
        return degreeWork.getFechaActual();
    }

    private boolean esEstadoRevisable(String estado) {
        return "PRIMERA_REVISION".equals(estado) || 
               "SEGUNDA_REVISION".equals(estado) || 
               "TERCERA_REVISION".equals(estado) ||
               "NO_ACEPTADO".equals(estado);
    }

    private void cargarAnteproyectos() {
    if (usuarioActual == null) {
        return;
    }

    try {
        System.out.println("DEBUG: Cargando anteproyectos para jefe de departamento: " + usuarioActual.getEmail());
        
        ResponseEntity<DegreeWork[]> response = apiService.get(
            "api/degreeworks", 
            "/listar/ANTEPROYECTO", 
            DegreeWork[].class
        );

        System.out.println("DEBUG: Status: " + response.getStatusCode());
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            DegreeWork[] anteproyectos = response.getBody();
            System.out.println("DEBUG: Anteproyectos recibidos: " + anteproyectos.length);
            
            // DEBUG DETALLADO: Ver TODOS los campos del DegreeWork
            for (int i = 0; i < anteproyectos.length; i++) {
                DegreeWork dw = anteproyectos[i];
                System.out.println("=== DEBUG DegreeWork " + i + " ===");
                System.out.println("ID: " + dw.getId());
                System.out.println("TituloProyecto: " + dw.getTituloProyecto());
                System.out.println("Titulo (sin Proyecto): " + dw.getTitulo()); // Por si acaso
                System.out.println("Director: " + (dw.getDirectorProyecto() != null ? 
                    dw.getDirectorProyecto().getEmail() + " (Nombre: " + dw.getDirectorProyecto().getFirstName() + ")" : "null"));
                System.out.println("Estudiantes size: " + (dw.getEstudiantes() != null ? dw.getEstudiantes().size() : "null"));
                
                if (dw.getEstudiantes() != null && !dw.getEstudiantes().isEmpty()) {
                    for (int j = 0; j < dw.getEstudiantes().size(); j++) {
                        User estudiante = dw.getEstudiantes().get(j);
                        System.out.println("  Estudiante " + j + ": " + estudiante.getEmail() + " - " + estudiante.getFirstName());
                    }
                }
                
                System.out.println("Fecha Actual: " + dw.getFechaActual());
                System.out.println("Estado DegreeWork: " + dw.getEstado());
                System.out.println("FormatosA size: " + (dw.getFormatosA() != null ? dw.getFormatosA().size() : "null"));
                System.out.println("Anteproyectos size: " + (dw.getAnteproyectos() != null ? dw.getAnteproyectos().size() : "null"));
                
                if (dw.getAnteproyectos() != null && !dw.getAnteproyectos().isEmpty()) {
                    for (int k = 0; k < dw.getAnteproyectos().size(); k++) {
                        Document doc = dw.getAnteproyectos().get(k);
                        System.out.println("  Anteproyecto " + k + ": Estado=" + doc.getEstado() + ", Fecha=" + doc.getFechaActual());
                    }
                }
                
                System.out.println("=== FIN DEBUG ===");
            }
            
            todosLosAnteproyectos = FXCollections.observableArrayList(anteproyectos);
            aplicarFiltro("Todos");
            
            System.out.println("DEBUG: Anteproyectos finales en tabla: " + todosLosAnteproyectos.size());
        } else {
            System.out.println("DEBUG: Respuesta no exitosa o body vacío");
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
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "ACEPTADO".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptado":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "NO_ACEPTADO".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Primera revisión":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "PRIMERA_REVISION".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda revisión":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "SEGUNDA_REVISION".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera revisión":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "TERCERA_REVISION".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Rechazado":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document anteproyecto = obtenerUltimoAnteproyecto(f);
                            return anteproyecto != null && anteproyecto.getEstado() != null && 
                                   "RECHAZADO".equalsIgnoreCase(anteproyecto.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            LocalDate fecha1 = obtenerFechaParaOrdenamiento(f1);
                            LocalDate fecha2 = obtenerFechaParaOrdenamiento(f2);
                            
                            if (fecha1 == null && fecha2 == null) return 0;
                            if (fecha1 == null) return 1;
                            if (fecha2 == null) return -1;
                            return fecha2.compareTo(fecha1);
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más antigua":
                tblAnteproyectos.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            LocalDate fecha1 = obtenerFechaParaOrdenamiento(f1);
                            LocalDate fecha2 = obtenerFechaParaOrdenamiento(f2);
                            
                            if (fecha1 == null && fecha2 == null) return 0;
                            if (fecha1 == null) return 1;
                            if (fecha2 == null) return -1;
                            return fecha1.compareTo(fecha2);
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