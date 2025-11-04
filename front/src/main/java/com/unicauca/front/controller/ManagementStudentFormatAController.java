package com.unicauca.front.controller;

import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Arrays;

@Controller
public class ManagementStudentFormatAController {

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
    
    private final ApiGatewayService apiService;
    private final NavigationController navigation;
    private User usuarioActual;
    private DegreeWork formatoActual;

    public ManagementStudentFormatAController(ApiGatewayService apiService, NavigationController navigation) {
        this.apiService = apiService;
        this.navigation = navigation;
    }

    @FXML
    private void initialize() {
        cargarFormatoA();
    }

    public void configurarConUsuario(User usuario) {
        this.usuarioActual = usuario;
        cargarFormatoA();
    }

    private void cargarFormatoA() {
        if (usuarioActual == null) {
            lblTituloValor.setText("Error: sin sesión activa");
            lblEstadoValor.setText("-");
            return;
        }

        try {
            //Obtener todos los proyectos y filtrar por estudiante
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
                    mostrarDatosFormato();
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

    private void mostrarDatosFormato() {
        try {
            //Usamos getters para obtener los datos del formato
            lblTituloValor.setText(formatoActual.getTituloProyecto() != null ? formatoActual.getTituloProyecto() : "-");
            lblModalidadValor.setText(formatoActual.getModalidad() != null ? formatoActual.getModalidad().toString() : "-");
            lblFechaValor.setText(formatoActual.getFechaActual() != null ? formatoActual.getFechaActual().toString() : "-");
            
            //Director 
            lblDirectorValor.setText(formatoActual.getDirectorProyecto() != null && 
                                   formatoActual.getDirectorProyecto().getEmail() != null ? 
                                   formatoActual.getDirectorProyecto().getEmail() : "-");
            
            //Codirector
            lblCodirectorValor.setText(formatoActual.getCodirectorProyecto() != null && 
                                     formatoActual.getCodirectorProyecto().getEmail() != null ? 
                                     formatoActual.getCodirectorProyecto().getEmail() : "-");

            //Objetivo General
            txtObjGeneral.setText(formatoActual.getObjetivoGeneral() != null ? formatoActual.getObjetivoGeneral() : "-");
            
            //Objetivos Específicos
            if (formatoActual.getObjetivosEspecificos() != null && !formatoActual.getObjetivosEspecificos().isEmpty()) {
                txtObjEspecificos.setText(String.join("\n• ", formatoActual.getObjetivosEspecificos()));
            } else {
                txtObjEspecificos.setText("-");
            }
            
            //Estado
            lblEstadoValor.setText(formatoActual.getEstado() != null ? formatoActual.getEstado().toString() : "Pendiente");
            
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error mostrando datos: " + e.getMessage());
        }
    }

    private void configurarBotonCorrecciones() {
        if (formatoActual == null) {
            btnVerCorrecciones.setDisable(true);
            return;
        }

        //Habilitar y deshabilitar boton de correciones
        String estado = formatoActual.getEstado() != null ? formatoActual.getEstado().toString() : "";
        boolean tieneCorrecciones = formatoActual.getCorrecciones() != null && 
                                   !formatoActual.getCorrecciones().trim().isEmpty();
        
        //Habilitar solo si el estado es "CORREGIDO" o "REVISADO" Y hay correcciones
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
        btnVerCorrecciones.setDisable(true);
    }

    private void mostrarError(String mensaje) {
        lblTituloValor.setText("Error cargando datos");
        lblEstadoValor.setText("-");
        System.out.println("Error en ManagementStudentFormatAController: " + mensaje);
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
    private void onBtnFormatoEstudianteClicked() {
        if (!"STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementStudentFormatA(usuarioActual);
    }

    @FXML
    private void onBtnAnteproyectoEstudianteClicked() {
        if (!"STUDENT".equalsIgnoreCase(usuarioActual.getRole())) {
            mostrarAlerta("Acceso denegado", "Solo los estudiantes pueden acceder a esta funcionalidad.", Alert.AlertType.WARNING);
            return;
        }
        navigation.showManagementStudentDraft(usuarioActual);
    }

    @FXML
    private void onBtnUsuarioClicked() {
        navigation.showPersonalInformation(usuarioActual);
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