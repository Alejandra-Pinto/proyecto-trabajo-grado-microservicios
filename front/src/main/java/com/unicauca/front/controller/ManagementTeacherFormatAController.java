package com.unicauca.front.controller;

import com.unicauca.front.dto.DegreeWorkDTO;
import com.unicauca.front.dto.DocumentDTO;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.Document;
import com.unicauca.front.model.EnumEstadoDocument;
import com.unicauca.front.model.EnumTipoDocumento;
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
import java.util.ArrayList;

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
                "/rol/PROFESSOR", 
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
            // ***** MODO EDICIÓN *****
            System.out.println("✏️ Modo EDICIÓN para trabajo ID: " + formato.getId());
            
            // Deshabilitar campo de estudiante en edición
            cbEstudiante.setDisable(true);
            cbEstudiante.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
            
            // Mostrar tooltip explicativo
            Tooltip tooltip = new Tooltip("Los estudiantes no se pueden cambiar después de creado el trabajo");
            cbEstudiante.setTooltip(tooltip);
            
            cargarDatosFormato(formato);
        } else {
            // ***** MODO CREACIÓN *****
            System.out.println("🆕 Modo CREACIÓN de nuevo trabajo");
            
            // Habilitar campo de estudiante en creación
            cbEstudiante.setDisable(false);
            cbEstudiante.setStyle("-fx-opacity: 1.0; -fx-background-color: white;");
            cbEstudiante.setTooltip(null);
            
            // Limpiar campos para nuevo formulario
            limpiarCamposCreacion();
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
            
            // Usar método helper para archivo PDF
            txtArchivoAdjunto.setText(obtenerArchivoPdf(formato));
            
            // Usar método helper para carta de aceptación
            if ("PRACTICA_PROFESIONAL".equals(cbModalidad.getValue())) {
                String carta = obtenerCartaAceptacion(formato);
                txtCartaAceptacion.setText(carta);
                lblCartaAceptacion.setVisible(true);
                hbCartaAceptacion.setVisible(true);
            } else {
                lblCartaAceptacion.setVisible(false);
                hbCartaAceptacion.setVisible(false);
                txtCartaAceptacion.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error cargando formato: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Método helper para obtener archivo PDF desde Formato A
    private String obtenerArchivoPdf(DegreeWork formato) {
        if (formato.getFormatosA() != null && !formato.getFormatosA().isEmpty()) {
            Document formatoA = formato.getFormatosA().get(0);
            return formatoA.getRutaArchivo() != null ? formatoA.getRutaArchivo() : "";
        }
        return "";
    }

    // Método helper para obtener carta de aceptación
    private String obtenerCartaAceptacion(DegreeWork formato) {
        if (formato.getCartasAceptacion() != null && !formato.getCartasAceptacion().isEmpty()) {
            Document carta = formato.getCartasAceptacion().get(0);
            return carta.getRutaArchivo() != null ? carta.getRutaArchivo() : "";
        }
        return "";
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
        boolean esActualizacion = (formatoActual != null && formatoActual.getId() > 0);
        
        if (!validarCamposObligatorios(esActualizacion)) {
            return;
        }
        
        try {
            // Crear DTO
            DegreeWorkDTO dto = new DegreeWorkDTO();
            
            // ***** DIFERENCIAR ENTRE CREACIÓN Y ACTUALIZACIÓN *****
            if (esActualizacion) {
                System.out.println("🔄 Enviando ACTUALIZACIÓN para trabajo ID: " + formatoActual.getId());
                // MODO ACTUALIZACIÓN: NO enviar estudiantes
                dto.setId((long) formatoActual.getId());
                
                // Solo enviar campos que se pueden actualizar
                String directorEmail = cbDirector.getValue();
                if (directorEmail != null && !directorEmail.isEmpty()) {
                    dto.setDirectorEmail(directorEmail);
                }
                
                // Configurar codirector (si existe)
                String codirectorEmail = cbCodirector.getValue();
                if (codirectorEmail != null && !codirectorEmail.isEmpty()) {
                    dto.setCodirectoresEmails(List.of(codirectorEmail));
                }
                
            } else {
                System.out.println("🆕 Enviando CREACIÓN de nuevo trabajo");
                // MODO CREACIÓN: ENVIAR estudiantes
                String estudianteEmail = cbEstudiante.getValue();
                if (estudianteEmail != null && !estudianteEmail.isEmpty()) {
                    dto.setEstudiantesEmails(List.of(estudianteEmail));
                } else {
                    mostrarAlerta("Error", "Debe seleccionar un estudiante", Alert.AlertType.ERROR);
                    return;
                }
                
                // Configurar director
                String directorEmail = cbDirector.getValue();
                if (directorEmail != null && !directorEmail.isEmpty()) {
                    dto.setDirectorEmail(directorEmail);
                }
                
                // Configurar codirector (si existe)
                String codirectorEmail = cbCodirector.getValue();
                if (codirectorEmail != null && !codirectorEmail.isEmpty()) {
                    dto.setCodirectoresEmails(List.of(codirectorEmail));
                }
            }
            
            // ***** CAMPOS COMUNES *****
            dto.setTitulo(txtTituloTrabajo.getText());
            
            // Convertir String a Enum
            try {
                dto.setModalidad(cbModalidad.getValue());
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Error", "Modalidad no válida", Alert.AlertType.ERROR);
                return;
            }
            
            dto.setFechaActual(dpFechaActual.getValue());
            dto.setObjetivoGeneral(txtObjetivoGeneral.getText());
            
            // Convertir objetivos específicos
            if (txtObjetivosEspecificos.getText() != null && !txtObjetivosEspecificos.getText().isEmpty()) {
                dto.setObjetivosEspecificos(Arrays.asList(txtObjetivosEspecificos.getText().split(";")));
            }
            
            // Estado inicial
            dto.setEstado("FORMATO_A");

            // ***** DOCUMENTOS - para creación Y actualización *****
            // Formato A (documento principal)
            if (txtArchivoAdjunto.getText() != null && !txtArchivoAdjunto.getText().isEmpty()) {
                DocumentDTO documentoDTO = new DocumentDTO();
                documentoDTO.setRutaArchivo(txtArchivoAdjunto.getText());
                
                // ¡¡¡ CAMBIO IMPORTANTE: Determinar estado dinámicamente !!!
                EnumEstadoDocument estadoDocumento = determinarEstadoDocumento(formatoActual);
                documentoDTO.setEstado(estadoDocumento);
                
                documentoDTO.setTipo(EnumTipoDocumento.FORMATO_A);
                dto.setFormatosA(List.of(documentoDTO));
                System.out.println("📄 Documento Formato A adjuntado: " + txtArchivoAdjunto.getText());
                System.out.println("📊 Estado enviado para el documento: " + estadoDocumento);
            }
            
            // Carta de aceptación (solo para práctica profesional)
            if ("PRACTICA_PROFESIONAL".equals(cbModalidad.getValue()) && 
                txtCartaAceptacion.getText() != null && !txtCartaAceptacion.getText().isEmpty()) {
                DocumentDTO cartaDTO = new DocumentDTO();
                cartaDTO.setRutaArchivo(txtCartaAceptacion.getText());
                cartaDTO.setEstado(EnumEstadoDocument.PRIMERA_REVISION);
                cartaDTO.setTipo(EnumTipoDocumento.CARTA_ACEPTACION);
                dto.setCartasAceptacion(List.of(cartaDTO));
                System.out.println("📄 Carta de aceptación adjuntada: " + txtCartaAceptacion.getText());
            }

            // ***** ENVIAR AL MICROSERVICIO *****
            ResponseEntity<DegreeWork> response;
            if (esActualizacion) {
                // Actualizar formato existente
                response = apiService.put(
                    "api/degreeworks", 
                    "/" + formatoActual.getId(), 
                    dto,
                    DegreeWork.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    mostrarAlerta("Éxito", "Formato actualizado correctamente", Alert.AlertType.INFORMATION);
                    // Volver a cargar el formato actualizado
                    if (response.getBody() != null) {
                        this.formatoActual = response.getBody();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el formato", Alert.AlertType.ERROR);
                }
                
            } else {
                // Crear nuevo formato
                response = apiService.post(
                    "api/degreeworks", 
                    "/registrar", 
                    dto,
                    DegreeWork.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    mostrarAlerta("Éxito", "Formato creado correctamente", Alert.AlertType.INFORMATION);
                    limpiarCampos();
                } else {
                    mostrarAlerta("Error", "No se pudo crear el formato", Alert.AlertType.ERROR);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Determina el estado correcto para un documento basado en el formato actual
     * - Si es nuevo: PRIMERA_REVISION
     * - Si ya existe y fue NO_ACEPTADO: SEGUNDA_REVISION, TERCERA_REVISION o RECHAZADO
     * - Si ya existe y fue ACEPTADO: Mantener ACEPTADO (no debería modificarse)
     */
    private EnumEstadoDocument determinarEstadoDocumento(DegreeWork formatoExistente) {
        if (formatoExistente == null) {
            // Si no hay formato existente, es creación nueva → PRIMERA_REVISION
            System.out.println("🆕 Creación nueva → PRIMERA_REVISION");
            return EnumEstadoDocument.PRIMERA_REVISION;
        }
        
        // Obtener el último Formato A del trabajo existente
        List<Document> formatosA = formatoExistente.getFormatosA();
        if (formatosA == null || formatosA.isEmpty()) {
            // Si no hay formatos A existentes, es primera vez → PRIMERA_REVISION
            System.out.println("📝 No hay formatos A existentes → PRIMERA_REVISION");
            return EnumEstadoDocument.PRIMERA_REVISION;
        }
        
        Document ultimoFormatoA = formatosA.get(formatosA.size() - 1);
        EnumEstadoDocument estadoActual = ultimoFormatoA.getEstado();
        
        System.out.println("🔍 Estado actual del último Formato A: " + estadoActual);
        
        // Si el documento actual está ACEPTADO, mantenerlo ACEPTADO (no se debería re-subir)
        if (estadoActual == EnumEstadoDocument.ACEPTADO) {
            System.out.println("✅ Documento ya ACEPTADO → Mantener ACEPTADO");
            return EnumEstadoDocument.ACEPTADO;
        }
        
        // Si el documento actual está RECHAZADO definitivamente, mantener RECHAZADO
        if (estadoActual == EnumEstadoDocument.RECHAZADO) {
            System.out.println("❌ Documento RECHAZADO definitivamente → Mantener RECHAZADO");
            return EnumEstadoDocument.RECHAZADO;
        }
        
        // Si el último documento fue NO_ACEPTADO, necesitamos determinar qué revisión sigue
        if (estadoActual == EnumEstadoDocument.NO_ACEPTADO) {
            // Contar cuántos documentos Formato A existen
            int totalFormatosA = formatosA.size();
            System.out.println("📊 Total de Formatos A existentes: " + totalFormatosA);
            
            // Determinar qué revisión corresponde según el número de intentos
            switch (totalFormatosA) {
                case 1:
                    // Primer intento fue NO_ACEPTADO → ahora es SEGUNDA_REVISION
                    System.out.println("🔄 Primer intento NO_ACEPTADO → SEGUNDA_REVISION");
                    return EnumEstadoDocument.SEGUNDA_REVISION;
                case 2:
                    // Segundo intento fue NO_ACEPTADO → ahora es TERCERA_REVISION
                    System.out.println("🔄 Segundo intento NO_ACEPTADO → TERCERA_REVISION");
                    return EnumEstadoDocument.TERCERA_REVISION;
                default:
                    // Tercer intento o más fue NO_ACEPTADO → RECHAZADO definitivo
                    System.out.println("🔄 Tercer intento o más NO_ACEPTADO → RECHAZADO");
                    return EnumEstadoDocument.RECHAZADO;
            }
        }
        
        // Si el documento está en alguna revisión (PRIMERA, SEGUNDA, TERCERA)
        // y se está actualizando, deberíamos mantener ese estado
        if (estadoActual == EnumEstadoDocument.PRIMERA_REVISION ||
            estadoActual == EnumEstadoDocument.SEGUNDA_REVISION ||
            estadoActual == EnumEstadoDocument.TERCERA_REVISION) {
            
            System.out.println("📝 Manteniendo estado actual de revisión: " + estadoActual);
            return estadoActual;
        }
        
        // Por defecto, PRIMERA_REVISION
        System.out.println("⚡ Estado no reconocido, usando PRIMERA_REVISION por defecto");
        return EnumEstadoDocument.PRIMERA_REVISION;
    }

    private boolean validarCamposObligatorios(boolean esActualizacion) {
        // Validación diferente para creación vs actualización
        
        if (esActualizacion) {
            // Para actualización, no validamos estudiante porque no se puede cambiar
            if (txtTituloTrabajo.getText().isEmpty() ||
                cbModalidad.getValue() == null || dpFechaActual.getValue() == null ||
                cbDirector.getValue() == null || txtObjetivoGeneral.getText().isEmpty() ||
                txtObjetivosEspecificos.getText().isEmpty()) {
                
                mostrarAlerta("Campos incompletos", "Por favor llene todos los campos obligatorios (*)", Alert.AlertType.WARNING);
                return false;
            }
        } else {
            // Para creación, validamos TODO incluyendo estudiante
            if (cbEstudiante.getValue() == null || txtTituloTrabajo.getText().isEmpty() ||
                cbModalidad.getValue() == null || dpFechaActual.getValue() == null ||
                cbDirector.getValue() == null || txtObjetivoGeneral.getText().isEmpty() ||
                txtObjetivosEspecificos.getText().isEmpty()) {
                
                mostrarAlerta("Campos incompletos", "Por favor llene todos los campos obligatorios (*)", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        // Validar que haya archivo adjunto
        if (txtArchivoAdjunto.getText().isEmpty()) {
            mostrarAlerta("Documento requerido", "Debe adjuntar el Formato A en formato PDF", Alert.AlertType.WARNING);
            return false;
        }
        
        // Validar carta de aceptación para práctica profesional
        if ("PRACTICA_PROFESIONAL".equals(cbModalidad.getValue()) && txtCartaAceptacion.getText().isEmpty()) {
            mostrarAlerta("Carta de aceptación requerida", "Para la modalidad de Práctica Profesional debe adjuntar la carta de aceptación", Alert.AlertType.WARNING);
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
            navigation.showPersonalInformation(usuarioActual);
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        navigation.showLogin();
    }

    @FXML
    private void onBtnFormatoDocenteClicked() {
        if (usuarioActual != null && "PROFESSOR".equalsIgnoreCase(usuarioActual.getRole())) {
            navigation.showPublishedTeacherFormatA(usuarioActual);
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
        // Limpiar completamente (para cuando se crea nuevo)
        cbEstudiante.setDisable(false);
        cbEstudiante.setStyle("-fx-opacity: 1.0; -fx-background-color: white;");
        cbEstudiante.setTooltip(null);
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
        lblCartaAceptacion.setVisible(false);
        hbCartaAceptacion.setVisible(false);
    }
    
    private void limpiarCamposCreacion() {
        // Solo limpiar campos de creación (no estado del formulario)
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
        lblCartaAceptacion.setVisible(false);
        hbCartaAceptacion.setVisible(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void deshabilitarCamposFijos() {
        cbEstudiante.setDisable(false);
        cbModalidad.setDisable(false);
        dpFechaActual.setDisable(false);
        cbDirector.setDisable(false);
        cbCodirector.setDisable(false);
    }
}