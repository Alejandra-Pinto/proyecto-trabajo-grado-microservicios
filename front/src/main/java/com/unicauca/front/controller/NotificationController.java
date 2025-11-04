package com.unicauca.front.controller;

import com.unicauca.front.model.Notification;
import com.unicauca.front.model.User;
import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

@Controller
public class NotificationController {

    @FXML private ListView<Notification> listViewNotifications;
    @FXML private Label lblUnreadCount;
    @FXML private Label lblEmpty;
    @FXML private Button btnRefresh;
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnClearAll;

    private final ApiGatewayService apiService;
    private User usuarioActual;
    private Timeline refreshTimeline;

    public NotificationController(ApiGatewayService apiService) {
        this.apiService = apiService;
    }

    @FXML
    private void initialize() {
        usuarioActual = SessionManager.getCurrentUser();
        configurarInterfaz();
        cargarNotificaciones();
        iniciarActualizacionAutomatica();
    }

    private void configurarInterfaz() {
        configurarListView();
        actualizarContador();
    }

    private void configurarListView() {
        listViewNotifications.setCellFactory(new Callback<ListView<Notification>, ListCell<Notification>>() {
            @Override
            public ListCell<Notification> call(ListView<Notification> param) {
                return new ListCell<Notification>() {
                    @Override
                    protected void updateItem(Notification notification, boolean empty) {
                        super.updateItem(notification, empty);
                        
                        if (empty || notification == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox container = new VBox(5);
                            
                            // Header con tipo y fecha
                            HBox header = new HBox(10);
                            Label lblType = new Label(notification.getType());
                            lblType.setStyle(getTypeStyle(notification.getType()));
                            
                            Label lblDate = new Label(formatDate(notification.getSentAt()));
                            lblDate.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10;");
                            
                            header.getChildren().addAll(lblType, lblDate);
                            
                            // Asunto
                            Label lblSubject = new Label(notification.getSubject());
                            lblSubject.setFont(Font.font("System", FontWeight.BOLD, 12));
                            
                            // Mensaje
                            Label lblMessage = new Label(notification.getMessage());
                            lblMessage.setWrapText(true);
                            lblMessage.setMaxWidth(300);
                            
                            // Footer con estado
                            HBox footer = new HBox(10);
                            Label lblStatus = new Label(notification.isRead() ? "✓ Leída" : "● No leída");
                            lblStatus.setStyle(notification.isRead() ? 
                                "-fx-text-fill: #28a745; -fx-font-size: 10;" : 
                                "-fx-text-fill: #dc3545; -fx-font-size: 10;");
                            
                            footer.getChildren().addAll(lblStatus);
                            
                            container.getChildren().addAll(header, lblSubject, lblMessage, footer);
                            
                            // Estilo del contenedor
                            String backgroundColor = getBackgroundColorByType(notification.getType());
                            container.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 5; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5;");
                            
                            setGraphic(container);
                        }
                    }
                };
            }
        });

        // Doble click para marcar como leída
        listViewNotifications.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Notification selected = listViewNotifications.getSelectionModel().getSelectedItem();
                if (selected != null && !selected.isRead()) {
                    selected.setRead(true);
                    listViewNotifications.refresh();
                    actualizarContador();
                }
            }
        });
    }

    private String getTypeStyle(String type) {
        return "-fx-background-color: " + getTypeColor(type) + "; -fx-text-fill: white; -fx-padding: 2 5; -fx-font-size: 10; -fx-background-radius: 3;";
    }

    private String getTypeColor(String type) {
        if (type == null) return "#6c757d";
        switch (type.toUpperCase()) {
            case "FORMATO_A_SUBIDO": return "#007bff";
            case "ANTEPROYECTO_SUBIDO": return "#28a745";
            case "EVALUACION": return "#ffc107";
            case "CORRECCION": return "#fd7e14";
            case "APROBACION": return "#20c997";
            case "RECHAZO": return "#dc3545";
            default: return "#6c757d";
        }
    }

    private String getBackgroundColorByType(String type) {
        if (type == null) return "#f8f9fa";
        switch (type.toUpperCase()) {
            case "FORMATO_A_SUBIDO": return "#e7f3ff";
            case "ANTEPROYECTO_SUBIDO": return "#e8f5e8";
            case "EVALUACION": return "#fff9e6";
            case "CORRECCION": return "#fff4e6";
            case "APROBACION": return "#e6f7f2";
            case "RECHAZO": return "#fde8e8";
            default: return "#f8f9fa";
        }
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @FXML
    private void cargarNotificaciones() {
        if (usuarioActual == null) return;

        Platform.runLater(() -> {
            try {
                // Usar ApiGatewayService para obtener notificaciones
                ResponseEntity<Notification[]> response = apiService.get(
                    "api/notifications", 
                    "", 
                    Notification[].class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Filtrar notificaciones por usuario
                    List<Notification> notificacionesUsuario = Arrays.stream(response.getBody())
                        .filter(notification -> usuarioActual.getEmail().equals(notification.getRecipientEmail()))
                        .toList();
                    
                    listViewNotifications.getItems().setAll(notificacionesUsuario);
                    actualizarContador();
                    lblEmpty.setVisible(notificacionesUsuario.isEmpty());
                }
                
            } catch (Exception e) {
                System.err.println("Error cargando notificaciones: " + e.getMessage());
                mostrarAlerta("Error", "No se pudieron cargar las notificaciones", Alert.AlertType.ERROR);
            }
        });
    }

    private void actualizarContador() {
        if (usuarioActual == null) return;
        
        long unreadCount = listViewNotifications.getItems().stream()
            .filter(notification -> !notification.isRead())
            .count();
        
        lblUnreadCount.setText(String.valueOf(unreadCount));
        
        // Actualizar badge en la aplicación principal si existe
        actualizarBadgeGlobal((int) unreadCount);
    }

    private void actualizarBadgeGlobal(int count) {
        // Aquí puedes implementar la actualización de un badge global
        // Por ejemplo, en el botón de notificaciones del Home
        System.out.println("Notificaciones no leídas: " + count);
    }

    @FXML
    private void onRefresh() {
        cargarNotificaciones();
    }

    @FXML
    private void onMarkAllRead() {
        listViewNotifications.getItems().forEach(notification -> notification.setRead(true));
        listViewNotifications.refresh();
        actualizarContador();
    }

    @FXML
    private void onClearAll() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar todas las notificaciones?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listViewNotifications.getItems().clear();
                actualizarContador();
                lblEmpty.setVisible(true);
            }
        });
    }

    private void iniciarActualizacionAutomatica() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> cargarNotificaciones()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void detenerActualizacion() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    // Método para agregar notificación en tiempo real
    public void agregarNotificacionEnTiempoReal(Notification notification) {
        Platform.runLater(() -> {
            if (usuarioActual.getEmail().equals(notification.getRecipientEmail())) {
                listViewNotifications.getItems().add(0, notification);
                actualizarContador();
                lblEmpty.setVisible(false);
                
                // Mostrar alerta toast
                mostrarToast(notification);
            }
        });
    }

    private void mostrarToast(Notification notification) {
        // Implementar un toast notification
        System.out.println("Nueva notificación: " + notification.getSubject());
        
        // Ejemplo simple de toast
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nueva notificación");
        alert.setHeaderText(notification.getSubject());
        alert.setContentText(notification.getMessage());
        alert.show();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}