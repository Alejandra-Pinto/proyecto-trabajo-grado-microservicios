package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Arrays;

@Controller
public class ManagementStudentDraftController {

    @FXML private Label lblTituloValor;
    @FXML private Label lblModalidadValor;
    @FXML private Label lblFechaValor;
    @FXML private Label lblDirectorValor;
    @FXML private Label lblCodirectorValor;
    @FXML private TextArea txtObjGeneral;
    @FXML private TextArea txtObjEspecificos;
    @FXML private Label lblEstadoValor;
    @FXML private Button btnVerCorrecciones;
    @FXML private ToggleButton btnUsuario;
    @FXML private ToggleButton btnFormatoPropuesta;
    @FXML private ToggleButton btnAnteproyecto;
    @FXML private TextField txtArchivoAnteproyecto;
    @FXML private Button btnAbrirAnteproyecto;
    
    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private DegreeWork formatoActual;
    private HostServices hostServices;

    public ManagementStudentDraftController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void initialize() {
        configurarInterfaz();
        cargarAnteproyecto();
    }

    private void configurarInterfaz() {
        // Deshabilitar el botón de anteproyecto (ya estamos en esa vista)
        //btnAnteproyecto.setDisable(true);
        //btnAnteproyecto.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        cargarAnteproyecto();
    }

    private void cargarAnteproyecto() {
        if (usuarioActual == null) {
            lblTituloValor.setText("Error: sin sesión activa");
            lblEstadoValor.setText("-");
            return;
        }

        try {
            // Obtener todos los proyectos y filtrar por estudiante
            ResponseEntity<DegreeWork[]> response = apiService.get("api/degreeworks", "", DegreeWork[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DegreeWork[] todosLosProyectos = response.getBody();
                
                // Filtrar por estudiante actual
                formatoActual = Arrays.stream(todosLosProyectos)
                    .filter(proyecto -> proyecto.getEstudiante() != null && 
                                       usuarioActual.getEmail().equals(proyecto.getEstudiante().getEmail()))
                    .findFirst()
                    .orElse(null);
                
                if (formatoActual != null) {
                    mostrarDatosAnteproyecto();
                    configurarBotonCorrecciones();
                } else {
                    mostrarDatosVacios();
                }
            } else {
                mostrarDatosVacios();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error cargando datos: " + e.getMessage());
            mostrarAlerta("Error de conexión", 
                         "No se pudo conectar con el servidor: " + e.getMessage(), 
                         Alert.AlertType.ERROR);
        }
    }

    private void mostrarDatosAnteproyecto() {
        try {
            // Usamos getters para obtener los datos del formato
            lblTituloValor.setText(formatoActual.getTituloProyecto() != null ? formatoActual.getTituloProyecto() : "-");
            lblModalidadValor.setText(formatoActual.getModalidad() != null ? formatoActual.getModalidad().toString() : "-");
            lblFechaValor.setText(formatoActual.getFechaActual() != null ? formatoActual.getFechaActual().toString() : "-");
            
            // Director 
            lblDirectorValor.setText(formatoActual.getDirectorProyecto() != null && 
                                   formatoActual.getDirectorProyecto().getEmail() != null ? 
                                   formatoActual.getDirectorProyecto().getEmail() : "-");
            
            // Codirector
            lblCodirectorValor.setText(formatoActual.getCodirectorProyecto() != null && 
                                     formatoActual.getCodirectorProyecto().getEmail() != null ? 
                                     formatoActual.getCodirectorProyecto().getEmail() : "-");

            // Objetivo General
            txtObjGeneral.setText(formatoActual.getObjetivoGeneral() != null ? formatoActual.getObjetivoGeneral() : "-");
            
            // Objetivos Específicos
            if (formatoActual.getObjetivosEspecificos() != null && !formatoActual.getObjetivosEspecificos().isEmpty()) {
                txtObjEspecificos.setText(String.join("\n• ", formatoActual.getObjetivosEspecificos()));
            } else {
                txtObjEspecificos.setText("-");
            }
            
            // Estado
            lblEstadoValor.setText(formatoActual.getEstado() != null ? formatoActual.getEstado().toString() : "Pendiente");
            
            // Cargar archivo de anteproyecto
            cargarArchivoAnteproyecto();
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error mostrando datos: " + e.getMessage());
        }
    }

    private void cargarArchivoAnteproyecto() {
        if (formatoActual.getAnteproyectos() != null && !formatoActual.getAnteproyectos().isEmpty()) {
            Document anteproyecto = formatoActual.getAnteproyectos().get(0);
            String rutaArchivo = anteproyecto.getRutaArchivo() != null ? anteproyecto.getRutaArchivo() : "";
            txtArchivoAnteproyecto.setText(rutaArchivo);
            
            // Habilitar botón de abrir si hay archivo
            btnAbrirAnteproyecto.setDisable(rutaArchivo.isEmpty());
        } else {
            txtArchivoAnteproyecto.setText("No hay anteproyecto cargado");
            btnAbrirAnteproyecto.setDisable(true);
        }
    }

    @FXML
    private void onAbrirAnteproyecto() {
        String ruta = txtArchivoAnteproyecto.getText();
        if (ruta == null || ruta.isEmpty() || ruta.equals("No hay anteproyecto cargado")) {
            mostrarAlerta("Sin archivo", "No hay ningún anteproyecto cargado.", Alert.AlertType.WARNING);
            return;
        }
        
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            mostrarAlerta("Archivo no encontrado", "El archivo no existe en la ruta especificada.", Alert.AlertType.ERROR);
            return;
        }
        
        if (hostServices != null) {
            hostServices.showDocument(archivo.toURI().toString());
        } else {
            mostrarAlerta("Error", "No se pudo abrir el archivo (HostServices no inicializado).", Alert.AlertType.ERROR);
        }
    }

    private void configurarBotonCorrecciones() {
        if (formatoActual == null) {
            btnVerCorrecciones.setDisable(true);
            return;
        }

        // Habilitar y deshabilitar boton de correciones
        String estado = formatoActual.getEstado() != null ? formatoActual.getEstado().toString() : "";
        boolean tieneCorrecciones = formatoActual.getCorrecciones() != null && 
                                   !formatoActual.getCorrecciones().trim().isEmpty();
        
        // Habilitar solo si el estado es "CORREGIDO" o "REVISADO" Y hay correcciones
        boolean habilitar = ("CORREGIDO".equalsIgnoreCase(estado) || 
                           "REVISADO".equalsIgnoreCase(estado)) && 
                           tieneCorrecciones;
        
        btnVerCorrecciones.setDisable(!habilitar);
    }

    private void mostrarDatosVacios() {
        lblTituloValor.setText("No hay trabajo registrado");
        lblModalidadValor.setText("-");
        lblFechaValor.setText("-");
        lblDirectorValor.setText("-");
        lblCodirectorValor.setText("-");
        txtObjGeneral.setText("El estudiante aún no ha subido el formato de grado.");
        txtObjEspecificos.setText("-");
        lblEstadoValor.setText("Pendiente");
        txtArchivoAnteproyecto.setText("No hay anteproyecto cargado");
        btnVerCorrecciones.setDisable(true);
        btnAbrirAnteproyecto.setDisable(true);
    }

    private void mostrarError(String mensaje) {
        lblTituloValor.setText("Error cargando datos");
        lblEstadoValor.setText("-");
        System.out.println("Error en ManagementStudentDraftController: " + mensaje);
    }

    @FXML
    private void onBtnVerCorreccionesClicked() {
        if (formatoActual != null) {
            navigation.showReviewStudentFormatA();
        } else {
            mostrarAlerta("Información", "No hay formato registrado para ver correcciones", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void onBtnUsuarioClicked() {
        if (usuarioActual != null) {
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void onBtnFormatoPropuestaClicked() {
        if (usuarioActual != null && "STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementStudentFormatA(usuarioActual);
        } else {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

        @FXML
    private void onBtnAnteproyectoClicked() {
        if (usuarioActual != null && "STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showManagementStudentDraft(usuarioActual);
        } else {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleLogout() {
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