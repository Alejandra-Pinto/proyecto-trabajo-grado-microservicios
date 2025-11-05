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
import javafx.util.Callback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
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
    private int contadorFormato = 1; // Para numeración consecutiva

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
            "Primera revisión",
            "Segunda revisión", 
            "Tercera revisión",
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
    // Número de formato (consecutivo)
    colNumeroFormato.setCellValueFactory(data -> {
        int numero = tblEstadosFormato.getItems().indexOf(data.getValue()) + 1;
        return new javafx.beans.property.SimpleStringProperty(String.valueOf(numero));
    });

    //Email del estudiante
    colEmailEstudiante.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        data.getValue().getEstudiante() != null && data.getValue().getEstudiante().getEmail() != null ? 
        data.getValue().getEstudiante().getEmail() : "Sin estudiante"
    ));

    //Fecha - Primero intenta obtener del último Formato A, si no usa la fecha del DegreeWork
    colFechaActual.setCellValueFactory(data -> {
        String fecha = "N/A";
        
        // Primero intentar con el último Formato A
        Document ultimoFormatoA = obtenerUltimoFormatoA(data.getValue());
        if (ultimoFormatoA != null && ultimoFormatoA.getFechaActual() != null) {
            fecha = ultimoFormatoA.getFechaActual().toString();
        } 
        // Si no hay Formato A, usar la fecha del DegreeWork
        else if (data.getValue().getFechaActual() != null) {
            fecha = data.getValue().getFechaActual().toString();
        }
        
        return new javafx.beans.property.SimpleStringProperty(fecha);
    });

    //Estado del último Formato A
    colEstado.setCellValueFactory(data -> {
        Document ultimoFormatoA = obtenerUltimoFormatoA(data.getValue());
        String estado = "Sin formato A";
        if (ultimoFormatoA != null && ultimoFormatoA.getEstado() != null) {
            estado = ultimoFormatoA.getEstado().toString();
        }
        return new javafx.beans.property.SimpleStringProperty(estado);
    });

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
                                Document ultimoFormatoA = obtenerUltimoFormatoA(formato);
                                if (ultimoFormatoA != null) {
                                    String estado = ultimoFormatoA.getEstado() != null ? ultimoFormatoA.getEstado().toString() : "";
                                    if ("NO_ACEPTADO".equals(estado) || "RECHAZADO".equals(estado)) {
                                        abrirVentanaCorrecciones(formato);
                                    } else {
                                        mostrarAlerta("Acción no permitida", 
                                            "Solo se pueden ver correcciones para estados 'No aceptado' o 'Rechazado'.", 
                                            Alert.AlertType.WARNING);
                                    }
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
                            Document ultimoFormatoA = obtenerUltimoFormatoA(formato);
                            if (ultimoFormatoA != null) {
                                String estado = ultimoFormatoA.getEstado() != null ? ultimoFormatoA.getEstado().toString() : "";
                                if ("NO_ACEPTADO".equals(estado) || "RECHAZADO".equals(estado)) {
                                    setGraphic(btnCorrections);
                                } else {
                                    setGraphic(null);
                                }
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    // Método helper para obtener el último Formato A
    private Document obtenerUltimoFormatoA(DegreeWork degreeWork) {
        if (degreeWork == null || degreeWork.getFormatosA() == null || degreeWork.getFormatosA().isEmpty()) {
            return null;
        }
        List<Document> formatosA = degreeWork.getFormatosA();
        // Obtener el último Formato A (el más reciente)
        return formatosA.get(formatosA.size() - 1);
    }

    private void cargarFormatosDelDocente() {
    if (usuarioActual == null) {
        return;
    }

    try {
        System.out.println("DEBUG: Cargando formatos para docente: " + usuarioActual.getEmail());
        
        ResponseEntity<DegreeWork[]> response = apiService.get(
            "api/degreeworks", 
            "/docente/" + usuarioActual.getEmail(), 
            DegreeWork[].class
        );

        System.out.println("DEBUG: Status: " + response.getStatusCode());
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            DegreeWork[] formatosArray = response.getBody();
            System.out.println("DEBUG: Formatos recibidos del backend: " + formatosArray.length);
            
            // DEBUG: Ver el contenido con información de documentos Y FECHAS
            for (DegreeWork dw : formatosArray) {
                Document ultimoFormatoA = obtenerUltimoFormatoA(dw);
                System.out.println("DEBUG - Formato: ID=" + dw.getId() + 
                    ", Titulo=" + dw.getTituloProyecto() + 
                    ", Fecha DegreeWork=" + dw.getFechaActual() +
                    ", Tiene FormatosA=" + (dw.getFormatosA() != null ? dw.getFormatosA().size() : 0) +
                    ", Fecha Ultimo FormatoA=" + (ultimoFormatoA != null ? ultimoFormatoA.getFechaActual() : "Ninguno") +
                    ", Estado FormatoA=" + (ultimoFormatoA != null ? ultimoFormatoA.getEstado() : "Ninguno"));
            }
            
            // USAR DIRECTAMENTE sin filtrar
            todosLosFormatos = FXCollections.observableArrayList(formatosArray);
            aplicarFiltro("Todos");
            
            System.out.println("DEBUG: Formatos finales en tabla: " + todosLosFormatos.size());
        } else {
            System.out.println("DEBUG: Respuesta no exitosa o body vacío");
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
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "ACEPTADO".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "NO_ACEPTADO".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Primera revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "PRIMERA_REVISION".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "SEGUNDA_REVISION".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "TERCERA_REVISION".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Rechazado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            Document formatoA = obtenerUltimoFormatoA(f);
                            return formatoA != null && formatoA.getEstado() != null && 
                                   "RECHAZADO".equalsIgnoreCase(formatoA.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            // Primero intentar con Formato A, luego con DegreeWork
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
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            // Primero intentar con Formato A, luego con DegreeWork
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
                tblEstadosFormato.getItems().setAll(base);
                break;
        }
    }


    // Método helper para obtener fecha para ordenamiento (primero Formato A, luego DegreeWork)
private LocalDate obtenerFechaParaOrdenamiento(DegreeWork degreeWork) {
    if (degreeWork == null) return null;
    
    // Primero intentar con el último Formato A
    Document ultimoFormatoA = obtenerUltimoFormatoA(degreeWork);
    if (ultimoFormatoA != null && ultimoFormatoA.getFechaActual() != null) {
        return ultimoFormatoA.getFechaActual();
    }
    
    // Si no hay Formato A, usar la fecha del DegreeWork
    return degreeWork.getFechaActual();
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
            navigation.showPersonalInformation(usuarioActual);
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