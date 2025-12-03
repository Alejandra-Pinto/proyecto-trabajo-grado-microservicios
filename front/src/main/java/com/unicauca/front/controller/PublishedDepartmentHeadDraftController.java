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
    @FXML private Button btnGuardar;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<DegreeWork> todosLosAnteproyectos;

    // Mapa para almacenar las celdas de acciones por índice de fila
    private final Map<Integer, TableCell<DegreeWork, Void>> celdasAcciones = new HashMap<>();
    // Mapa para almacenar temporalmente las selecciones de evaluadores
    private final Map<Long, List<String>> seleccionesTemporales = new HashMap<>();

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

        if (btnGuardar != null) {
            btnGuardar.setOnAction(event -> guardarAsignaciones());
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
        
        // Configurar botón Guardar si existe
        if (btnGuardar != null) {
            btnGuardar.setOnAction(event -> guardarAsignaciones());
        }

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

        // Columna de acciones (ComboBox de evaluadores)
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final ComboBox<String> comboEvaluador1 = new ComboBox<>();
                    private final ComboBox<String> comboEvaluador2 = new ComboBox<>();
                    private final HBox hbox = new HBox(5);
                    private final Label lblAsignar = new Label("Asignar:");
                    private ObservableList<String> todosLosEvaluadores = FXCollections.observableArrayList();
                    private boolean evaluadoresCargados = false;
                    
                    {
                        // Configurar ComboBox más compactos
                        comboEvaluador1.setPromptText("Evaluador 1");
                        comboEvaluador2.setPromptText("Evaluador 2");
                        comboEvaluador1.setPrefWidth(130);
                        comboEvaluador2.setPrefWidth(130);
                        comboEvaluador1.setMaxWidth(130);
                        comboEvaluador2.setMaxWidth(130);
                        
                        // Estilo más compacto
                        lblAsignar.setStyle("-fx-font-size: 10px; -fx-padding: 0 5 0 0;");
                        hbox.setStyle("-fx-alignment: center-left; -fx-padding: 2;");
                        
                        hbox.getChildren().addAll(lblAsignar, comboEvaluador1, comboEvaluador2);
                        
                        // Listeners para sincronizar los ComboBox y guardar selecciones
                        comboEvaluador1.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                actualizarOpcionesComboBox2();
                            }
                            guardarSeleccionTemporal();
                        });
                        
                        comboEvaluador2.valueProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                actualizarOpcionesComboBox1();
                            }
                            guardarSeleccionTemporal();
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                            evaluadoresCargados = false;
                        } else {
                            DegreeWork anteproyecto = getTableRow().getItem();
                            int index = getIndex();
                            
                            // Guardar referencia a esta celda
                            celdasAcciones.put(index, this);
                            
                            // Cargar evaluadores solo la primera vez que se muestra la celda
                            if (!evaluadoresCargados) {
                                cargarEvaluadores();
                                evaluadoresCargados = true;
                            }
                            
                            setGraphic(hbox);
                        }
                    }
                    
                    // Métodos para acceder a los ComboBox
                    public String getEvaluador1() {
                        return comboEvaluador1.getValue();
                    }
                    
                    public String getEvaluador2() {
                        return comboEvaluador2.getValue();
                    }
                    
                    private void guardarSeleccionTemporal() {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            DegreeWork anteproyecto = getTableRow().getItem();
                            List<String> evaluadores = new ArrayList<>();
                            
                            String eval1 = getEvaluador1();
                            String eval2 = getEvaluador2();
                            
                            if (eval1 != null && !eval1.trim().isEmpty() && 
                                !eval1.contains("No hay evaluadores") && 
                                !eval1.startsWith("Error: ")) {
                                String email = extraerEmailEvaluador(eval1);
                                if (email != null) {
                                    evaluadores.add(email);
                                }
                            }
                            
                            if (eval2 != null && !eval2.trim().isEmpty() && 
                                !eval2.contains("No hay evaluadores") && 
                                !eval2.startsWith("Error: ")) {
                                String email = extraerEmailEvaluador(eval2);
                                if (email != null) {
                                    evaluadores.add(email);
                                }
                            }
                            
                            if (!evaluadores.isEmpty()) {
                                seleccionesTemporales.put(anteproyecto.getId(), evaluadores);
                                System.out.println("DEBUG: Guardada selección temporal para anteproyecto ID " + 
                                                 anteproyecto.getId() + ": " + evaluadores);
                            } else {
                                // Si no hay evaluadores seleccionados, remover del mapa
                                seleccionesTemporales.remove(anteproyecto.getId());
                            }
                        }
                    }
                    
                    private void cargarEvaluadores() {
                        try {
                            System.out.println("DEBUG: Intentando cargar evaluadores asignados...");
                            
                            ResponseEntity<User[]> response = apiService.get(
                                "api/usuarios", 
                                "/evaluadores", 
                                User[].class
                            );
                            
                            System.out.println("DEBUG: Status code: " + response.getStatusCode());
                            System.out.println("DEBUG: ¿Tiene body?: " + (response.getBody() != null));
                            
                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                User[] evaluadores = response.getBody();
                                System.out.println("DEBUG: Evaluadores recibidos: " + evaluadores.length);
                                
                                // Crear lista de nombres completos para mejor identificación
                                List<String> nombres = Arrays.stream(evaluadores)
                                    .map(user -> user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")")
                                    .collect(Collectors.toList());
                                
                                todosLosEvaluadores = FXCollections.observableArrayList(nombres);
                                
                                // Inicializar ambos ComboBox con todas las opciones
                                comboEvaluador1.setItems(todosLosEvaluadores);
                                comboEvaluador2.setItems(todosLosEvaluadores);
                                
                                System.out.println("DEBUG: ComboBox actualizados con " + nombres.size() + " evaluadores");
                            } else {
                                System.out.println("DEBUG: No se pudieron cargar los evaluadores");
                                comboEvaluador1.setItems(FXCollections.observableArrayList("No hay evaluadores"));
                                comboEvaluador2.setItems(FXCollections.observableArrayList("No hay evaluadores"));
                            }
                        } catch (Exception e) {
                            System.out.println("ERROR cargando evaluadores: " + e.getMessage());
                            e.printStackTrace();
                            comboEvaluador1.setItems(FXCollections.observableArrayList("Error: " + e.getMessage()));
                            comboEvaluador2.setItems(FXCollections.observableArrayList("Error: " + e.getMessage()));
                        }
                    }
                    
                    private void actualizarOpcionesComboBox2() {
                        String seleccionado1 = comboEvaluador1.getValue();
                        if (seleccionado1 != null) {
                            // Filtrar opciones para combo2: todas excepto la seleccionada en combo1
                            ObservableList<String> opcionesCombo2 = todosLosEvaluadores.filtered(
                                evaluador -> !evaluador.equals(seleccionado1)
                            );
                            comboEvaluador2.setItems(opcionesCombo2);
                            
                            // Si combo2 tenía seleccionado el mismo que ahora seleccionó combo1, limpiarlo
                            if (seleccionado1.equals(comboEvaluador2.getValue())) {
                                comboEvaluador2.setValue(null);
                            }
                        } else {
                            // Si no hay selección en combo1, mostrar todas las opciones en combo2
                            comboEvaluador2.setItems(todosLosEvaluadores);
                        }
                    }
                    
                    private void actualizarOpcionesComboBox1() {
                        String seleccionado2 = comboEvaluador2.getValue();
                        if (seleccionado2 != null) {
                            // Filtrar opciones para combo1: todas excepto la seleccionada en combo2
                            ObservableList<String> opcionesCombo1 = todosLosEvaluadores.filtered(
                                evaluador -> !evaluador.equals(seleccionado2)
                            );
                            comboEvaluador1.setItems(opcionesCombo1);
                            
                            // Si combo1 tenía seleccionado el mismo que ahora seleccionó combo2, limpiarlo
                            if (seleccionado2.equals(comboEvaluador1.getValue())) {
                                comboEvaluador1.setValue(null);
                            }
                        } else {
                            // Si no hay selección en combo2, mostrar todas las opciones en combo1
                            comboEvaluador1.setItems(todosLosEvaluadores);
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

        // Limpiar el mapa de celdas cuando se aplica filtro
        celdasAcciones.clear();
        
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
    
    @FXML
    private void guardarAsignaciones() {
        System.out.println("DEBUG: Iniciando guardado de asignaciones...");
        
        if (tblAnteproyectos.getItems() == null || tblAnteproyectos.getItems().isEmpty()) {
            mostrarAlerta("Información", "No hay anteproyectos para guardar asignaciones", Alert.AlertType.INFORMATION);
            return;
        }
        
        // Usar las selecciones temporales que se han estado guardando
        if (seleccionesTemporales.isEmpty()) {
            mostrarAlerta("Información", 
                "No hay evaluadores seleccionados para guardar.\n\n" +
                "Por favor, seleccione al menos un evaluador en la columna 'Acciones' " +
                "usando los ComboBox en cada fila de anteproyecto.", 
                Alert.AlertType.INFORMATION);
            return;
        }
        
        System.out.println("DEBUG: Total de anteproyectos con asignaciones: " + seleccionesTemporales.size());
        
        // Mostrar confirmación con detalles
        StringBuilder detalles = new StringBuilder();
        for (Map.Entry<Long, List<String>> entry : seleccionesTemporales.entrySet()) {
            DegreeWork anteproyecto = encontrarAnteproyectoPorId(entry.getKey());
            String titulo = anteproyecto != null && anteproyecto.getTituloProyecto() != null ? 
                           anteproyecto.getTituloProyecto() : "ID: " + entry.getKey();
            detalles.append("• ").append(titulo).append(": ")
                   .append(entry.getValue().size()).append(" evaluador(es)\n");
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Guardado");
        confirmacion.setHeaderText("Guardar asignaciones de evaluadores");
        confirmacion.setContentText("¿Está seguro de guardar las asignaciones para " + 
                                  seleccionesTemporales.size() + " anteproyecto(s)?\n\n" +
                                  "Detalles:\n" + detalles.toString());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            guardarAsignacionesEnServidor(new HashMap<>(seleccionesTemporales));
        }
    }
    
    private DegreeWork encontrarAnteproyectoPorId(Long id) {
        if (todosLosAnteproyectos == null) return null;
        return todosLosAnteproyectos.stream()
            .filter(dw -> dw.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private String extraerEmailEvaluador(String textoCompleto) {
        // El formato es: "Nombre Apellido (email@dominio.com)"
        int inicioParentesis = textoCompleto.indexOf('(');
        int finParentesis = textoCompleto.indexOf(')');
        
        if (inicioParentesis != -1 && finParentesis != -1 && inicioParentesis < finParentesis) {
            String email = textoCompleto.substring(inicioParentesis + 1, finParentesis).trim();
            return email;
        }
        
        return textoCompleto; // Si no está en el formato esperado, devolver el texto completo
    }

    private void guardarAsignacionesEnServidor(Map<Long, List<String>> asignacionesPorAnteproyecto) {
    try {
        int exitosas = 0;
        int fallidas = 0;
        List<String> errores = new ArrayList<>();
        
        // Obtener todos los evaluadores con sus datos completos
        List<User> todosLosEvaluadores = obtenerEvaluadoresCompletos();
        if (todosLosEvaluadores == null || todosLosEvaluadores.isEmpty()) {
            mostrarAlerta("Error", "No se pudieron cargar los evaluadores", Alert.AlertType.ERROR);
            return;
        }
        
        // Crear un mapa de email -> User completo
        Map<String, User> emailToUserMap = new HashMap<>();
        for (User evaluador : todosLosEvaluadores) {
            if (evaluador.getEmail() != null) {
                emailToUserMap.put(evaluador.getEmail(), evaluador);
            }
        }
        
        for (Map.Entry<Long, List<String>> entry : asignacionesPorAnteproyecto.entrySet()) {
            Long degreeWorkId = entry.getKey();
            List<String> emailsEvaluadores = entry.getValue();
            
            System.out.println("DEBUG: Enviando asignación para anteproyecto ID " + degreeWorkId + 
                             " - Emails: " + emailsEvaluadores);
            
            try {
                // Buscar los objetos User completos basados en los emails
                List<User> usuariosCompletos = new ArrayList<>();
                boolean todosEncontrados = true;
                
                for (String email : emailsEvaluadores) {
                    User usuario = emailToUserMap.get(email);
                    if (usuario != null) {
                        usuariosCompletos.add(usuario);
                        System.out.println("DEBUG: Encontrado usuario para email " + email + 
                                         " - ID: " + usuario.getId() + 
                                         " - Nombre: " + usuario.getFirstName());
                    } else {
                        System.out.println("ERROR: No se encontró usuario para email: " + email);
                        todosEncontrados = false;
                        break;
                    }
                }
                
                if (!todosEncontrados) {
                    fallidas++;
                    String errorMsg = "Anteproyecto ID " + degreeWorkId + ": No se encontraron todos los usuarios";
                    errores.add(errorMsg);
                    continue;
                }
                
                System.out.println("DEBUG: Usuarios a enviar: " + usuariosCompletos.size());
                
                // Llamar al endpoint del microservicio de evaluaciones
                ResponseEntity<?> response = apiService.post(
                    "api/evaluaciones", // Microservicio de evaluaciones
                    "/" + degreeWorkId + "/asignar-evaluadores", // Endpoint específico
                    usuariosCompletos, // Cuerpo con lista de objetos User completos
                    Object.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    exitosas++;
                    System.out.println("DEBUG: Asignación exitosa para anteproyecto ID " + degreeWorkId);
                } else {
                    fallidas++;
                    String errorMsg = "Anteproyecto ID " + degreeWorkId + ": " + response.getStatusCode();
                    errores.add(errorMsg);
                    System.out.println("ERROR: " + errorMsg);
                }
                
            } catch (Exception e) {
                fallidas++;
                String errorMsg = "Anteproyecto ID " + degreeWorkId + ": " + e.getMessage();
                errores.add(errorMsg);
                System.out.println("ERROR: Excepción al asignar evaluadores - " + errorMsg);
                e.printStackTrace();
            }
        }
        
        // Mostrar resumen
        mostrarResumenGuardado(exitosas, fallidas, errores);
        
        // Si hubo al menos una asignación exitosa, refrescar la tabla
        if (exitosas > 0) {
            seleccionesTemporales.clear();
            cargarAnteproyectos();
        }
        
    } catch (Exception e) {
        mostrarAlerta("Error", "Error al guardar las asignaciones: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    }
}

private List<User> obtenerEvaluadoresCompletos() {
    try {
        System.out.println("DEBUG: Obteniendo evaluadores completos...");
        
        ResponseEntity<User[]> response = apiService.get(
            "api/usuarios", 
            "/evaluadores", 
            User[].class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            User[] evaluadores = response.getBody();
            System.out.println("DEBUG: Evaluadores completos recibidos: " + evaluadores.length);
            
            // DEBUG: Mostrar información de cada evaluador
            for (User evaluador : evaluadores) {
                System.out.println("DEBUG - Evaluador completo: " + 
                    "ID: " + evaluador.getId() + 
                    ", Email: " + evaluador.getEmail() + 
                    ", Nombre: " + evaluador.getFirstName() + " " + evaluador.getLastName() +
                    ", Rol: " + evaluador.getRole());
            }
            
            return Arrays.asList(evaluadores);
        }
    } catch (Exception e) {
        System.out.println("ERROR obteniendo evaluadores completos: " + e.getMessage());
        e.printStackTrace();
    }
    
    return null;
}

private void mostrarResumenGuardado(int exitosas, int fallidas, List<String> errores) {
    StringBuilder mensaje = new StringBuilder();
    mensaje.append("Resultado del guardado:\n")
           .append("• Asignaciones exitosas: ").append(exitosas).append("\n")
           .append("• Asignaciones fallidas: ").append(fallidas).append("\n")
           .append("• Total procesados: ").append(exitosas + fallidas).append("\n");
    
    if (!errores.isEmpty()) {
        mensaje.append("\nErrores encontrados:\n");
        for (String error : errores) {
            mensaje.append("• ").append(error).append("\n");
        }
    }
    
    Alert.AlertType tipoAlerta = exitosas > 0 ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
    Alert resultado = new Alert(tipoAlerta);
    resultado.setTitle("Resultado del Guardado");
    resultado.setHeaderText("Proceso de asignación completado");
    resultado.setContentText(mensaje.toString());
    resultado.showAndWait();
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