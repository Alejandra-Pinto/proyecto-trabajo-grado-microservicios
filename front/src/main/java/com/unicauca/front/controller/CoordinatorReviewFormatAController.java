package com.unicauca.front.controller;

import com.unicauca.front.dto.EvaluationRequestDTO;
import com.unicauca.front.dto.EvaluationResponseDTO;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
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
            } else {
                txtCorrecciones.setDisable(true);
            }
        });
    }

    public void setFormato(DegreeWork formato) {
        this.formato = formato;
        if (formato != null) {
            cargarDatosFormato();
        }
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
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
                
                // Cargar evaluaciones existentes para este documento
                cargarEvaluacionesDocumento(formatoA.getId());
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

    private void cargarEvaluacionesDocumento(Long documentoId) {
        try {
            ResponseEntity<EvaluationResponseDTO[]> response = apiService.get(
                "api/evaluations", 
                "?documentId=" + documentoId, 
                EvaluationResponseDTO[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                EvaluationResponseDTO[] evaluaciones = response.getBody();
                
                if (evaluaciones.length > 0) {
                    // Tomar la última evaluación
                    EvaluationResponseDTO ultimaEvaluacion = evaluaciones[evaluaciones.length - 1];
                    
                    // Actualizar UI basado en la evaluación
                    actualizarUIDesdeEvaluacion(ultimaEvaluacion);
                }
            }

        } catch (Exception e) {
            System.err.println("Error cargando evaluaciones: " + e.getMessage());
            // No interrumpir la carga principal por error en evaluaciones
        }
    }

    private void actualizarUIDesdeEvaluacion(EvaluationResponseDTO evaluacion) {
        // Actualizar estado en combobox
        switch (evaluacion.getResultado()) {
            case "APROBADO":
                cmbEstado.setValue("ACEPTADO");
                break;
            case "NO_APROBADO":
                cmbEstado.setValue("NO ACEPTADO");
                break;
            case "RECHAZADO":
                cmbEstado.setValue("RECHAZADO");
                break;
        }
        
        // Cargar correcciones si existen
        if (evaluacion.getCorrecciones() != null && !evaluacion.getCorrecciones().isEmpty()) {
            txtCorrecciones.setText(evaluacion.getCorrecciones());
        }
        
        // Habilitar/deshabilitar controles según estado
        boolean puedeEvaluar = true; // O tu lógica específica
        btnEnviar.setDisable(!puedeEvaluar);
        cmbEstado.setDisable(!puedeEvaluar);
    }







    //------------------------------

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
            // Obtener el primer formato A
            if (formato.getFormatosA().isEmpty()) {
                mostrarAlerta("Error", "No se encontró el documento Formato A.", Alert.AlertType.ERROR);
                return;
            }

            Document formatoA = formato.getFormatosA().get(0);
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

            // Crear evaluación usando el email del evaluador
            EvaluationRequestDTO evaluationRequest = new EvaluationRequestDTO();
            evaluationRequest.setDocumentId(formatoA.getId());
            evaluationRequest.setEvaluadorEmail(usuarioActual.getEmail()); // Usar email en lugar de ID
            evaluationRequest.setCorrecciones(txtCorrecciones.getText());
            evaluationRequest.setTipo("FORMATO_A");

            // Mapear estado seleccionado al resultado
            switch (estadoSeleccionado) {
                case "ACEPTADO":
                    evaluationRequest.setResultado("APROBADO");
                    break;
                case "NO ACEPTADO":
                    evaluationRequest.setResultado("NO_APROBADO");
                    break;
                case "RECHAZADO":
                    evaluationRequest.setResultado("RECHAZADO");
                    break;
            }

            // Enviar al microservicio de evaluaciones
            boolean exito = enviarEvaluacionAlBackend(evaluationRequest);

            if (exito) {
                mostrarAlerta("Éxito", "Evaluación enviada exitosamente.", Alert.AlertType.INFORMATION);
                navigation.showManagementCoordinatorFormatA();
            } else {
                mostrarAlerta("Error", "No se pudo enviar la evaluación.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al enviar evaluación: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean enviarEvaluacionAlBackend(EvaluationRequestDTO request) {
        try {
            ResponseEntity<EvaluationResponseDTO> response = apiService.post(
                "api/evaluations", 
                "", 
                request, 
                EvaluationResponseDTO.class
            );

            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean actualizarFormato(DegreeWork formatoActualizado) {
        try {
            //Enviar el objeto DegreeWork completo como JSON (igual que en RegisterController)
            ResponseEntity<DegreeWork> response = apiService.put(
                "api/degreeworks", 
                "/" + formato.getId(), 
                formatoActualizado, 
                DegreeWork.class
            );

            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation();
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
