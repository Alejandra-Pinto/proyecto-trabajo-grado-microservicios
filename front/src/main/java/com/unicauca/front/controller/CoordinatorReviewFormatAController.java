package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import com.unicauca.front.model.EstadoFormatoA;
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

    private void cargarDatosFormato() {
        if (formato == null) return;

        try {
            String estudiante = formato.getEstudiante() != null ? formato.getEstudiante().getEmail() : "";
            String modalidad = formato.getModalidad() != null ? formato.getModalidad().toString() : "";
            lblEstudiante.setText(estudiante + (modalidad.isEmpty() ? "" : " - " + modalidad));

            //Archivo principal (Formato A)
            String ruta = formato.getArchivoPdf() != null ? formato.getArchivoPdf() : "";
            txtArchivoAdjunto.setText(ruta);

            //Si modalidad es PRACTICA_PROFESIONAL mostramos la carta
            if ("PRACTICA_PROFESIONAL".equalsIgnoreCase(modalidad)) {
                String carta = formato.getCartaAceptacionEmpresa() != null ? formato.getCartaAceptacionEmpresa() : "";

                lblCartaEmpresa.setVisible(true);
                txtCartaEmpresa.setVisible(true);
                btnAbrirCartaEmpresa.setVisible(true);
                txtCartaEmpresa.setText(carta);
            } else {
                lblCartaEmpresa.setVisible(false);
                txtCartaEmpresa.setVisible(false);
                btnAbrirCartaEmpresa.setVisible(false);
            }

            //Cargar datos del formato
            lblCargarTitulo.setText(formato.getTituloProyecto() != null ? formato.getTituloProyecto() : "No hay trabajo registrado");
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

            //Habilitar RECHAZADO si tiene 3 intentos fallidos
            if (formato.getNoAprobadoCount() >= 3) {
                if (!cmbEstado.getItems().contains("RECHAZADO")) {
                    cmbEstado.getItems().add("RECHAZADO");
                }
            }

            //Habilitar/deshabilitar controles según estado actual
            String estadoActual = formato.getEstado() != null ? formato.getEstado().toString() : "";
            boolean puedeEvaluar = "PRIMERA_EVALUACION".equals(estadoActual) || 
                                 "SEGUNDA_EVALUACION".equals(estadoActual) || 
                                 "TERCERA_EVALUACION".equals(estadoActual) || 
                                 "NO_ACEPTADO".equals(estadoActual);

            btnEnviar.setDisable(!puedeEvaluar);
            cmbEstado.setDisable(!puedeEvaluar);

            //Cargar correcciones existentes si las hay
            if (formato.getCorrecciones() != null && !formato.getCorrecciones().trim().isEmpty()) {
                txtCorrecciones.setText(formato.getCorrecciones());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando datos del formato: " + e.getMessage(), Alert.AlertType.ERROR);
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
            //Crear una copia del formato para enviar al microservicio
            DegreeWork formatoActualizado = new DegreeWork();
            formatoActualizado.setId(formato.getId()); // Mantener el mismo ID
            
            //Copiar todos los campos existentes
            formatoActualizado.setEstudiante(formato.getEstudiante());
            formatoActualizado.setDirectorProyecto(formato.getDirectorProyecto());
            formatoActualizado.setCodirectorProyecto(formato.getCodirectorProyecto());
            formatoActualizado.setTituloProyecto(formato.getTituloProyecto());
            formatoActualizado.setModalidad(formato.getModalidad());
            formatoActualizado.setFechaActual(formato.getFechaActual());
            formatoActualizado.setObjetivoGeneral(formato.getObjetivoGeneral());
            formatoActualizado.setObjetivosEspecificos(formato.getObjetivosEspecificos());
            formatoActualizado.setArchivoPdf(formato.getArchivoPdf());
            formatoActualizado.setCartaAceptacionEmpresa(formato.getCartaAceptacionEmpresa());

            boolean exito = false;

            switch (estadoSeleccionado) {
                case "ACEPTADO":
                    //Actualizar estado a ACEPTADO
                    formatoActualizado.setEstado(EstadoFormatoA.ACEPTADO);
                    formatoActualizado.setNoAprobadoCount(0); // Reiniciar contador
                    exito = actualizarFormato(formatoActualizado);
                    break;

                case "NO ACEPTADO":
                    String correcciones = txtCorrecciones.getText();
                    if (correcciones == null || correcciones.trim().isEmpty()) {
                        mostrarAlerta("Advertencia", "Debes escribir correcciones para un NO ACEPTADO.", Alert.AlertType.WARNING);
                        return;
                    }
                    //Guardar correcciones y actualizar estado
                    formatoActualizado.setCorrecciones(correcciones);
                    formatoActualizado.setEstado(EstadoFormatoA.NO_ACEPTADO);
                    formatoActualizado.setNoAprobadoCount(formato.getNoAprobadoCount() + 1); // Incrementar contador
                    exito = actualizarFormato(formatoActualizado);
                    break;

                case "RECHAZADO":
                    //Actualizar estado a RECHAZADO
                    formatoActualizado.setEstado(EstadoFormatoA.RECHAZADO);
                    formatoActualizado.setNoAprobadoCount(formato.getNoAprobadoCount());
                    exito = actualizarFormato(formatoActualizado);
                    break;
            }

            if (exito) {
                mostrarAlerta("Éxito", "Correcciones enviadas y estado actualizado.", Alert.AlertType.INFORMATION);
                //Actualizar el formato local con los cambios
                this.formato = formatoActualizado;
                //Regresar a la vista anterior
                navigation.showManagementCoordinatorFormatA();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al enviar correcciones: " + e.getMessage(), Alert.AlertType.ERROR);
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
