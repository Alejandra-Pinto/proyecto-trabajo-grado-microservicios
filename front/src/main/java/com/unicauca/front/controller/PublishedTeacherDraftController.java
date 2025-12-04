/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
public class PublishedTeacherDraftController {

    @FXML private AnchorPane mainAnchorPane;
    @FXML private TableView<DegreeWork> tblEstadosFormato;
    @FXML private TableColumn<DegreeWork, String> colNumeroAnteproyecto;
    @FXML private TableColumn<DegreeWork, String> colEmailEstudiante;
    @FXML private TableColumn<DegreeWork, String> colFechaActual;
    @FXML private TableColumn<DegreeWork, String> colEstado;
    @FXML private TableColumn<DegreeWork, Void> colAcciones;
    @FXML private ComboBox<String> comboClasificar;
    @FXML private ToggleButton btnRol;
    @FXML private ToggleButton btnFormatoDocente;
    @FXML private ToggleButton btnAnteproyectoDocente;
    @FXML private Button btnAgregarAnteproyecto;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private ObservableList<DegreeWork> todosLosAnteproyectos;

    public PublishedTeacherDraftController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            cargarAnteproyectosDelDocente();
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null && "PROFESSOR".equalsIgnoreCase(usuario.getRole())) {
            configurarBotonesDocente();
            cargarAnteproyectosDelDocente();
        }
    }

    private void configurarInterfaz() {
        // Configurar ComboBox de filtros - EXACTAMENTE IGUAL
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
        // Nombre del anteproyecto (en lugar de número consecutivo)
        colNumeroAnteproyecto.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTituloProyecto() != null ? 
                data.getValue().getTituloProyecto() : "Sin título"
            )
        );

        // Email del estudiante - EXACTAMENTE IGUAL
        colEmailEstudiante.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEstudiante() != null && 
                data.getValue().getEstudiante().getEmail() != null ? 
                data.getValue().getEstudiante().getEmail() : "Sin estudiante"
            )
        );

        // Fecha - EXACTAMENTE IGUAL (pero usando fecha del DegreeWork directamente)
        colFechaActual.setCellValueFactory(data -> {
            String fecha = "N/A";
            if (data.getValue().getFechaActual() != null) {
                fecha = data.getValue().getFechaActual().toString();
            }
            return new javafx.beans.property.SimpleStringProperty(fecha);
        });

        // Estado del anteproyecto - EXACTAMENTE IGUAL (pero usando estado del DegreeWork)
        colEstado.setCellValueFactory(data -> {
            String estado = "Sin estado";
            if (data.getValue().getEstado() != null) {
                estado = data.getValue().getEstado().toString();
            }
            return new javafx.beans.property.SimpleStringProperty(estado);
        });

        // Columna de acciones (botón "Ver Correcciones") - EXACTAMENTE IGUAL
        colAcciones.setCellFactory(new Callback<TableColumn<DegreeWork, Void>, TableCell<DegreeWork, Void>>() {
            @Override
            public TableCell<DegreeWork, Void> call(final TableColumn<DegreeWork, Void> param) {
                return new TableCell<DegreeWork, Void>() {
                    private final Button btnCorrections = new Button("Ver Correcciones");

                    {
                        btnCorrections.setStyle("-fx-background-color: #111F63; -fx-text-fill: white; -fx-padding: 5;");
                        btnCorrections.setOnAction(event -> {
                            DegreeWork anteproyecto = getTableView().getItems().get(getIndex());
                            if (anteproyecto != null) {
                                String estado = anteproyecto.getEstado() != null ? anteproyecto.getEstado().toString() : "";
                                // EXACTAMENTE IGUAL
                                if ("NO_ACEPTADO".equals(estado) || "RECHAZADO".equals(estado)) {
                                    abrirVentanaCorrecciones(anteproyecto);
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
                            DegreeWork anteproyecto = getTableRow().getItem();
                            String estado = anteproyecto.getEstado() != null ? anteproyecto.getEstado().toString() : "";
                            // EXACTAMENTE IGUAL
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

    private void cargarAnteproyectosDelDocente() {
        if (usuarioActual == null) {
            return;
        }

        try {
            System.out.println("DEBUG: Cargando anteproyectos para docente: " + usuarioActual.getEmail());
            
            // Ajusta el endpoint según tu API
            ResponseEntity<DegreeWork[]> response = apiService.get(
                "api/degreeworks", 
                "/docente/anteproyectos/" + usuarioActual.getEmail(), 
                DegreeWork[].class
            );

            System.out.println("DEBUG: Status: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] anteproyectosArray = response.getBody();
                System.out.println("DEBUG: Anteproyectos recibidos: " + anteproyectosArray.length);
                
                // DEBUG: Ver el contenido
                for (DegreeWork dw : anteproyectosArray) {
                    System.out.println("DEBUG - Anteproyecto: ID=" + dw.getId() + 
                        ", Titulo=" + dw.getTituloProyecto() + 
                        ", Estado=" + dw.getEstado() +
                        ", Fecha=" + dw.getFechaActual());
                }
                
                todosLosAnteproyectos = FXCollections.observableArrayList(anteproyectosArray);
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
                tblEstadosFormato.getItems().setAll(base);
                break;

            case "Aceptado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "ACEPTADO".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "No aceptado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "NO_ACEPTADO".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Primera revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "PRIMERA_REVISION".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Segunda revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "SEGUNDA_REVISION".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Tercera revisión":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "TERCERA_REVISION".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Rechazado":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .filter(f -> {
                            // EXACTAMENTE IGUAL
                            return f.getEstado() != null && 
                                   "RECHAZADO".equalsIgnoreCase(f.getEstado().toString());
                        })
                        .collect(Collectors.toList())
                );
                break;

            case "Fecha más reciente":
                tblEstadosFormato.getItems().setAll(
                    base.stream()
                        .sorted((f1, f2) -> {
                            // EXACTAMENTE IGUAL
                            LocalDate fecha1 = f1.getFechaActual();
                            LocalDate fecha2 = f2.getFechaActual();
                            
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
                            // EXACTAMENTE IGUAL
                            LocalDate fecha1 = f1.getFechaActual();
                            LocalDate fecha2 = f2.getFechaActual();
                            
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

    // Método helper para obtener fecha para ordenamiento - EXACTAMENTE IGUAL (pero simplificado)
    private LocalDate obtenerFechaParaOrdenamiento(DegreeWork degreeWork) {
        if (degreeWork == null) return null;
        return degreeWork.getFechaActual();
    }

    private void abrirVentanaCorrecciones(DegreeWork anteproyecto) {
        if (anteproyecto != null) {
            // Navegar a la vista de correcciones del docente para anteproyectos
            navigation.showTeacherReviewFormatA(usuarioActual, anteproyecto);
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
            navigation.showPublishedTeacherFormatA(usuarioActual);
        }
    }

    @FXML
    private void onBtnAnteproyectoDocenteClicked() {
        // Ya estamos en la vista de anteproyectos, no hacer nada o recargar
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            cargarAnteproyectosDelDocente();
        }
    }

    @FXML
    private void onAgregarAnteproyecto() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherDraft(usuarioActual);
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