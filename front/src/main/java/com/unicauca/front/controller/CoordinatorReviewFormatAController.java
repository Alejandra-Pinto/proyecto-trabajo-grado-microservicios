package com.unicauca.front.controller;

import com.unicauca.front.dto.ActualizarEvaluacionDTO;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.EnumEstadoDocument;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class CoordinatorReviewFormatAController {

    private HostServices hostServices;
    private DegreeWork formato;
    private static final String BASE_PATH = "Documents";

    @FXML private Label lblEstudiante;
    @FXML private TextField txtArchivoAdjunto;
    @FXML private Button btnAbrirArchivo;
    @FXML private Label lblCartaEmpresa;
    @FXML private TextField txtCartaEmpresa;
    @FXML private Button btnAbrirCartaEmpresa;
    @FXML private TextArea txtCorrecciones;
    @FXML private Label lblCargarTitulo;
    @FXML private Label lblCargarModalidad;
    @FXML private Label lblCargarFecha;
    @FXML private Label lblCargarDirector;
    @FXML private Label lblCargarCodirector;
    @FXML private Label lblCargarObjetivoGeneral;
    @FXML private Label lblCargarObjetivosEspecificos;
    @FXML private Button btnEnviar;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ToggleButton btnUsuario;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;

    public CoordinatorReviewFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
    }

    private void configurarInterfaz() {
        txtArchivoAdjunto.setEditable(false);
        txtCartaEmpresa.setEditable(false);

        //Configuramos ComboBox de estados
        cmbEstado.getItems().clear();
        cmbEstado.getItems().addAll("ACEPTADO", "NO ACEPTADO");

        //Personalizamos estilos de cada opción
        cmbEstado.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "ACEPTADO":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "NO ACEPTADO":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case "RECHAZADO":
                            setStyle("-fx-text-fill: darkred; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        cmbEstado.setButtonCell(cmbEstado.getCellFactory().call(null));
        cmbEstado.getSelectionModel().clearSelection();
        txtCorrecciones.setDisable(true);

        cmbEstado.setOnAction(e -> {
            String selected = cmbEstado.getValue();
            if ("NO ACEPTADO".equals(selected)) {
                txtCorrecciones.setDisable(false);
                txtCorrecciones.setPromptText("Escriba las correcciones necesarias...");
            } else {
                txtCorrecciones.setDisable(true);
                txtCorrecciones.clear();
                txtCorrecciones.setPromptText("");
            }
        });
    }

    public void setFormato(DegreeWork formato) {
        this.formato = formato;
        if (formato != null) {
            System.out.println("Cargando formato ID: " + formato.getId() + " para revisión");
            cargarDatosFormato();
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            btnUsuario.setText("Coordinador: " + usuario.getFirstName());
        }
    }
    
    //-------------------------------
    //cargar datos del formato en la interfaz
    private void cargarDatosFormato() {
        if (formato == null) return;

        try {
            String estudiante = formato.getEstudiante() != null ? formato.getEstudiante().getEmail() : "";
            String modalidad = formato.getModalidad() != null ? formato.getModalidad().toString() : "";
            lblEstudiante.setText(estudiante + (modalidad.isEmpty() ? "" : " - " + modalidad));

            // Cargar información del documento (Formato A)
            if (!formato.getFormatosA().isEmpty()) {
                Document formatoA = formato.getFormatosA().get(0);
                String ruta = formatoA.getRutaArchivo() != null ? formatoA.getRutaArchivo() : "";
                txtArchivoAdjunto.setText(ruta);
                
                // Cargar estado desde el documento (no desde evaluaciones)
                cargarEstadoDesdeDocumento(formatoA);
            }

            // Cargar carta de aceptación si aplica
            if ("PRACTICA_PROFESIONAL".equalsIgnoreCase(modalidad) && !formato.getCartasAceptacion().isEmpty()) {
                Document cartaDoc = formato.getCartasAceptacion().get(0);
                String carta = cartaDoc.getRutaArchivo() != null ? cartaDoc.getRutaArchivo() : "";
                
                lblCartaEmpresa.setVisible(true);
                txtCartaEmpresa.setVisible(true);
                btnAbrirCartaEmpresa.setVisible(true);
                txtCartaEmpresa.setText(carta);
            } else {
                lblCartaEmpresa.setVisible(false);
                txtCartaEmpresa.setVisible(false);
                btnAbrirCartaEmpresa.setVisible(false);
            }

            // Cargar datos básicos del formato
            cargarDatosBasicosFormato();

            // Habilitar RECHAZADO si tiene 3 intentos fallidos
            if (formato.getNoAprobadoCount() >= 3) {
                if (!cmbEstado.getItems().contains("RECHAZADO")) {
                    cmbEstado.getItems().add("RECHAZADO");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando datos del formato: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarEstadoDesdeDocumento(Document formatoA) {
        try {
            if (formatoA.getEstado() != null) {
                String estado = formatoA.getEstado().toString();
                System.out.println("Estado del documento: " + estado);
                
                // Mapear estado del documento a estado en combobox
                switch (estado.toUpperCase()) {
                    case "ACEPTADO":
                        cmbEstado.setValue("ACEPTADO");
                        txtCorrecciones.setDisable(true);
                        break;
                    case "RECHAZADO":
                        cmbEstado.setValue("RECHAZADO");
                        txtCorrecciones.setDisable(true);
                        break;
                    case "NO_ACEPTADO":
                    case "EN_CORRECCION":
                        cmbEstado.setValue("NO ACEPTADO");
                        txtCorrecciones.setDisable(false);
                        // Cargar correcciones si existen en el trabajo de grado
                        if (formato.getCorrecciones() != null && !formato.getCorrecciones().isEmpty()) {
                            txtCorrecciones.setText(formato.getCorrecciones());
                        }
                        break;
                    default:
                        // Estado pendiente o no definido
                        System.out.println("Estado no reconocido: " + estado + ", manteniendo selección vacía");
                        cmbEstado.getSelectionModel().clearSelection();
                        txtCorrecciones.setDisable(true);
                }
            } else {
                System.out.println("Documento sin estado definido");
                cmbEstado.getSelectionModel().clearSelection();
                txtCorrecciones.setDisable(true);
            }
        } catch (Exception e) {
            System.err.println("Error cargando estado del documento: " + e.getMessage());
        }
    }

    private void cargarDatosBasicosFormato() {
        lblCargarTitulo.setText(formato.getTituloProyecto() != null ? formato.getTituloProyecto() : "No hay trabajo registrado");
        
        String modalidad = formato.getModalidad() != null ? formato.getModalidad().toString() : "";
        lblCargarModalidad.setText(modalidad.isEmpty() ? "No disponible" : modalidad);
        
        lblCargarFecha.setText(formato.getFechaActual() != null ? formato.getFechaActual().toString() : "No registrada");
        lblCargarDirector.setText(formato.getDirectorProyecto() != null ? formato.getDirectorProyecto().getEmail() : "No definido");
        lblCargarCodirector.setText(formato.getCodirectorProyecto() != null ? formato.getCodirectorProyecto().getEmail() : "No definido");
        lblCargarObjetivoGeneral.setText(formato.getObjetivoGeneral() != null ? formato.getObjetivoGeneral() : "No definido");
        
        if (formato.getObjetivosEspecificos() != null && !formato.getObjetivosEspecificos().isEmpty()) {
            lblCargarObjetivosEspecificos.setText(String.join("; ", formato.getObjetivosEspecificos()));
        } else {
            lblCargarObjetivosEspecificos.setText("No disponibles");
        }
    }

    @FXML
    private void onAbrirArchivo() {
        String ruta = txtArchivoAdjunto.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Sin archivo", "No hay ningún archivo adjunto para abrir.", Alert.AlertType.WARNING);
            return;
        }

        File archivo = new File("Documents", ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en: " + archivo.getAbsolutePath(), Alert.AlertType.ERROR);
            return;
        }

        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
        } else {
            mostrarAlerta("Error", "No se pudo abrir el archivo (HostServices no inicializado).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAbrirCartaEmpresa() {
        String ruta = txtCartaEmpresa.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Sin archivo", "No hay carta de aceptación adjunta.", Alert.AlertType.WARNING);
            return;
        }

        File archivo = new File("Documents", ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en: " + archivo.getAbsolutePath(), Alert.AlertType.ERROR);
            return;
        }

        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
        } else {
            mostrarAlerta("Error", "No se pudo abrir el archivo (HostServices no inicializado).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onEnviarCorrecciones() {
        if (formato == null) {
            mostrarAlerta("Error", "No hay un formato cargado.", Alert.AlertType.ERROR);
            return;
        }

        String estadoSeleccionado = cmbEstado.getValue();
        if (estadoSeleccionado == null || estadoSeleccionado.isEmpty()) {
            mostrarAlerta("Advertencia", "Debes seleccionar un estado.", Alert.AlertType.WARNING);
            return;
        }

        try {
            User usuarioActual = SessionManager.getCurrentUser();
            
            if (usuarioActual == null) {
                mostrarAlerta("Error", "No hay usuario autenticado.", Alert.AlertType.ERROR);
                return;
            }

            // Validar correcciones si es "NO ACEPTADO"
            if ("NO ACEPTADO".equals(estadoSeleccionado)) {
                String correcciones = txtCorrecciones.getText();
                if (correcciones == null || correcciones.trim().isEmpty()) {
                    mostrarAlerta("Advertencia", "Debes escribir correcciones para un NO ACEPTADO.", Alert.AlertType.WARNING);
                    return;
                }
            }

            System.out.println("Enviando evaluación para trabajo ID: " + formato.getId());
            System.out.println("Estado seleccionado: " + estadoSeleccionado);
            System.out.println("Correcciones: " + txtCorrecciones.getText());

            // Enviar evaluación usando PATCH (el endpoint que SÍ existe)
            boolean exito = actualizarEstadoFormato(estadoSeleccionado);

            if (exito) {
                mostrarAlerta("Éxito", "Evaluación enviada exitosamente.", Alert.AlertType.INFORMATION);
                // Regresar a la lista de formatos
                navigation.showManagementCoordinatorFormatA();
            } else {
                mostrarAlerta("Error", "No se pudo enviar la evaluación.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al enviar evaluación: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean actualizarEstadoFormato(String estado) {
        try {
            if (formato == null || formato.getId() == null) {
                System.err.println("No se puede actualizar: formato o ID es null");
                return false;
            }

            // Crear DTO para actualizar el trabajo de grado
            ActualizarEvaluacionDTO dto = new ActualizarEvaluacionDTO();
            dto.setDegreeWorkId(formato.getId());
            dto.setObservaciones(txtCorrecciones.getText());
            
            // Mapear estado seleccionado a EnumEstadoDocument
            EnumEstadoDocument estadoEnum = null;
            switch (estado) {
                case "ACEPTADO":
                    estadoEnum = EnumEstadoDocument.ACEPTADO;
                    break;
                case "NO ACEPTADO":
                    estadoEnum = EnumEstadoDocument.NO_ACEPTADO;
                    break;
                case "RECHAZADO":
                    estadoEnum = EnumEstadoDocument.RECHAZADO;
                    break;
            }
            
            if (estadoEnum == null) {
                System.err.println("Estado no válido: " + estado);
                return false;
            }
            
            dto.setEstado(estadoEnum);

            System.out.println("➡ Enviando DTO a /api/evaluaciones/evaluacion:");
            System.out.println("DegreeWorkId: " + dto.getDegreeWorkId());
            System.out.println("Estado: " + dto.getEstado());
            System.out.println("Observaciones: " + dto.getObservaciones());

            ResponseEntity<Object> response = apiService.patch(
                "api/evaluaciones",  // ✅ Usar el endpoint que SÍ existe
                "/evaluacion",
                dto,
                Object.class
            );

            System.out.println("Respuesta del PATCH: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Evaluación actualizada correctamente");
                return true;
            } else {
                System.err.println("Error en la respuesta: " + response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error actualizando evaluación: " + e.getMessage());
            e.printStackTrace();
            return false;
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
    private void onBtnEvaluarPropuestasClicked() {
        if (usuarioActual != null && "COORDINATOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementCoordinatorFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los coordinadores pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToManagement() {
        navigation.showManagementCoordinatorFormatA();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);

        Label etiqueta = new Label(mensaje);
        etiqueta.setWrapText(true);
        etiqueta.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        VBox contenedor = new VBox(etiqueta);
        contenedor.setSpacing(10);
        contenedor.setPadding(new Insets(10));

        alerta.getDialogPane().setContent(contenedor);
        alerta.showAndWait();
    }
}