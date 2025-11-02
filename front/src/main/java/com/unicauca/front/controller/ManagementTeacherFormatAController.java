package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Modalidad;
import com.unicauca.front.model.Student;
import com.unicauca.front.model.Teacher;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
public class ManagementTeacherFormatAController {

    //Botones principales
    @FXML private Button btnAdjuntarDocumento;
    @FXML private ToggleButton btnUsuario;

    //Campos de formulario
    @FXML private ComboBox<String> cbEstudiante;
    @FXML private ComboBox<String> cbDirector;
    @FXML private ComboBox<String> cbCodirector;
    @FXML private TextField txtTituloTrabajo;
    @FXML private ComboBox<String> cbModalidad;
    @FXML private DatePicker dpFechaActual;
    @FXML private TextArea txtObjetivoGeneral;
    @FXML private TextArea txtObjetivosEspecificos;
    @FXML private TextField txtArchivoAdjunto;
    @FXML private Label lblCartaAceptacion;
    @FXML private HBox hbCartaAceptacion;
    @FXML private TextField txtCartaAceptacion;
    @FXML private Button btnAdjuntarCarta;
    @FXML private Button btnAbrirCarta;

    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private File archivoAdjunto;
    private DegreeWork formatoActual;
    private HostServices hostServices;

    public ManagementTeacherFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarCombos();
        configurarListeners();
    }

    private void configurarCombos() {
        //Configurar modalidades
        cbModalidad.getItems().setAll("INVESTIGACION", "PRACTICA_PROFESIONAL");

        try {
            //Cargar estudiantes desde microservicio
            ResponseEntity<User[]> responseEstudiantes = apiService.get(
                "api/usuarios", 
                "/rol/STUDENT", 
                User[].class
            );
            
            if (responseEstudiantes.getStatusCode().is2xxSuccessful() && responseEstudiantes.getBody() != null) {
                List<String> emailsEstudiantes = Arrays.stream(responseEstudiantes.getBody())
                    .map(User::getEmail)
                    .toList();
                cbEstudiante.getItems().setAll(emailsEstudiantes);
            }

            //Cargar profesores desde microservicio
            ResponseEntity<User[]> responseProfesores = apiService.get(
                "api/usuarios", 
                "/rol/TEACHER", 
                User[].class
            );
            
            if (responseProfesores.getStatusCode().is2xxSuccessful() && responseProfesores.getBody() != null) {
                List<String> emailsProfesores = Arrays.stream(responseProfesores.getBody())
                    .map(User::getEmail)
                    .toList();
                cbDirector.getItems().setAll(emailsProfesores);
                cbCodirector.getItems().setAll(emailsProfesores);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando datos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarListeners() {
        cbModalidad.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("PRACTICA_PROFESIONAL".equals(newVal)) {
                lblCartaAceptacion.setVisible(true);
                hbCartaAceptacion.setVisible(true);
            } else {
                lblCartaAceptacion.setVisible(false);
                hbCartaAceptacion.setVisible(false);
                txtCartaAceptacion.clear();
            }
        });
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            btnUsuario.setText("Docente: " + usuario.getFirstName());
        }
    }

    public void configurarConFormato(DegreeWork formato) {
        this.formatoActual = formato;
        if (formato != null) {
            cargarDatosFormato(formato);
        }
    }

    private void cargarDatosFormato(DegreeWork formato) {
        try {
            cbEstudiante.setValue(formato.getEstudiante() != null ? formato.getEstudiante().getEmail() : "");
            cbDirector.setValue(formato.getDirectorProyecto() != null ? formato.getDirectorProyecto().getEmail() : "");
            
            if (formato.getCodirectorProyecto() != null) {
                cbCodirector.setValue(formato.getCodirectorProyecto().getEmail());
            }
            
            txtTituloTrabajo.setText(formato.getTituloProyecto() != null ? formato.getTituloProyecto() : "");
            
            if (formato.getModalidad() != null) {
                cbModalidad.setValue(formato.getModalidad().toString());
            }
            
            dpFechaActual.setValue(formato.getFechaActual() != null ? formato.getFechaActual() : LocalDate.now());
            txtObjetivoGeneral.setText(formato.getObjetivoGeneral() != null ? formato.getObjetivoGeneral() : "");
            
            if (formato.getObjetivosEspecificos() != null && !formato.getObjetivosEspecificos().isEmpty()) {
                txtObjetivosEspecificos.setText(String.join(";", formato.getObjetivosEspecificos()));
            }
            
            txtArchivoAdjunto.setText(formato.getArchivoPdf() != null ? formato.getArchivoPdf() : "");
            
            if ("PRACTICA_PROFESIONAL".equals(cbModalidad.getValue()) && formato.getCartaAceptacionEmpresa() != null) {
                txtCartaAceptacion.setText(formato.getCartaAceptacionEmpresa());
                lblCartaAceptacion.setVisible(true);
                hbCartaAceptacion.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando formato: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onAdjuntarDocumento() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar documento");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Files", "*.docx"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(null);
        if (archivoSeleccionado != null) {
            txtArchivoAdjunto.setText(archivoSeleccionado.getAbsolutePath());
            archivoAdjunto = archivoSeleccionado;
            mostrarAlerta("Documento cargado", "Archivo seleccionado: " + archivoSeleccionado.getName(), Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onAbrirArchivo() {
        String ruta = txtArchivoAdjunto.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Sin archivo", "No hay ningún archivo seleccionado.", Alert.AlertType.WARNING);
            return;
        }
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en la ruta especificada.", Alert.AlertType.ERROR);
            return;
        }
        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
        }
    }

    @FXML
    private void onGuardarFormato() {
        if (!validarCamposObligatorios()) {
            return;
        }
        try {
            DegreeWork formato = new DegreeWork();
            
            //Usar Student (que hereda de User)
            Student estudiante = new Student();
            estudiante.setEmail(cbEstudiante.getValue());
            formato.setEstudiante(estudiante);
            
            //Usar Teacher (que hereda de User)  
            Teacher director = new Teacher();
            director.setEmail(cbDirector.getValue());
            formato.setDirectorProyecto(director);
            
            if (cbCodirector.getValue() != null && !cbCodirector.getValue().isEmpty()) {
                Teacher codirector = new Teacher();
                codirector.setEmail(cbCodirector.getValue());
                formato.setCodirectorProyecto(codirector);
            }
            
            //Resto de campos
            formato.setTituloProyecto(txtTituloTrabajo.getText());
            
            //Convertir String a Enum
            try {
                formato.setModalidad(Modalidad.valueOf(cbModalidad.getValue()));
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Error", "Modalidad no válida", Alert.AlertType.ERROR);
                return;
            }
            
            formato.setFechaActual(dpFechaActual.getValue());
            formato.setObjetivoGeneral(txtObjetivoGeneral.getText());
            formato.setObjetivosEspecificos(Arrays.asList(txtObjetivosEspecificos.getText().split(";")));
            formato.setArchivoPdf(txtArchivoAdjunto.getText());
            
            if ("PRACTICA_PROFESIONAL".equals(cbModalidad.getValue())) {
                formato.setCartaAceptacionEmpresa(txtCartaAceptacion.getText());
            }

            //Enviar al microservicio
            ResponseEntity<DegreeWork> response;
            if (formatoActual != null && formatoActual.getId() > 0) {
                //Actualizar formato existente
                formato.setId(formatoActual.getId());
                response = apiService.put(
                    "api/degreeworks", 
                    "/" + formatoActual.getId(), 
                    formato,
                    DegreeWork.class
                );
            } else {
                //Crear nuevo formato
                response = apiService.post(
                    "api/degreeworks", 
                    "/registrar", 
                    formato,
                    DegreeWork.class
                );
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                mostrarAlerta("Éxito", "Formato guardado correctamente", Alert.AlertType.INFORMATION);
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo guardar el formato", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
    }

}

    private boolean validarCamposObligatorios() {
        if (cbEstudiante.getValue() == null || txtTituloTrabajo.getText().isEmpty() ||
            cbModalidad.getValue() == null || dpFechaActual.getValue() == null ||
            cbDirector.getValue() == null || txtObjetivoGeneral.getText().isEmpty() ||
            txtObjetivosEspecificos.getText().isEmpty() || txtArchivoAdjunto.getText().isEmpty()) {
            
            mostrarAlerta("Campos incompletos", "Por favor llene todos los campos obligatorios (*)", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void onAdjuntarCarta() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar carta de aceptación");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Files", "*.docx"),
            new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(null);
        if (archivoSeleccionado != null) {
            txtCartaAceptacion.setText(archivoSeleccionado.getAbsolutePath());
            mostrarAlerta("Carta cargada", "Carta de aceptación seleccionada: " + archivoSeleccionado.getName(), Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onAbrirCarta() {
        String ruta = txtCartaAceptacion.getText();
        if (ruta == null || ruta.isEmpty()) {
            mostrarAlerta("Sin archivo", "No hay ninguna carta seleccionada.", Alert.AlertType.WARNING);
            return;
        }
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en la ruta especificada.", Alert.AlertType.ERROR);
            return;
        }
        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
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
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "TEACHER".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementTeacherFormatA();
        } else {
            mostrarAlerta("Acceso denegado", "Solo los docentes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleBackToHome() {
        if (usuarioActual != null) {
            navigation.showHomeWithUser(usuarioActual);
        }
    }

    private void limpiarCampos() {
        cbEstudiante.getSelectionModel().clearSelection();
        txtTituloTrabajo.clear();
        cbModalidad.getSelectionModel().clearSelection();
        dpFechaActual.setValue(LocalDate.now());
        cbDirector.getSelectionModel().clearSelection();
        cbCodirector.getSelectionModel().clearSelection();
        txtObjetivoGeneral.clear();
        txtObjetivosEspecificos.clear();
        txtArchivoAdjunto.clear();
        txtCartaAceptacion.clear();
        archivoAdjunto = null;
        formatoActual = null;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void deshabilitarCamposFijos() {
        cbEstudiante.setDisable(true);
        cbModalidad.setDisable(true);
        dpFechaActual.setDisable(true);
        cbDirector.setDisable(true);
        cbCodirector.setDisable(true);
    }

}