package com.unicauca.front.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.unicauca.front.model.User;
import com.unicauca.front.controller.HomeController;

import java.io.IOException;

@Component
public class NavigationController {
    private final ApplicationContext applicationContext;
    private Stage primaryStage;

    public NavigationController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Métodos de navegación para CADA una de tus pantallas
    public void showLogin() {
        loadFXML("/fxml/Login.fxml", "Iniciar Sesión");
    }

    public void showRegister() {
        loadFXML("/fxml/Register.fxml", "Registrar Usuario");
    }

    public void showHomeAdmin() {
        loadFXML("/fxml/HomeAdmin.fxml", "Panel de Administración");
    }

    public void showHome() {
        loadFXML("/fxml/Home.fxml", "Inicio");
    }

    public void showManagementAdmin() {
        loadFXML("/fxml/ManagementAdmin.fxml", "Gestión Administradores");
    }

    public void showManagementStudentFormatA() {
        loadFXML("/fxml/ManagementStudentFormatA.fxml", "Gestión Estudiantes Formato A");
    }

    public void showManagementTeacherFormatA() {
        loadFXML("/fxml/ManagementTeacherFormatA.fxml", "Gestión Profesores Formato A");
    }

    public void showManagementCoordinatorFormatA() {
        loadFXML("/fxml/ManagementCoordinatorFormatA.fxml", "Gestión Coordinadores Formato A");
    }

    public void showReviewStudentFormatA() {
        loadFXML("/fxml/ReviewStudentFormatA.fxml", "Revisión Estudiantes Formato A");
    }

    public void showReviewTeacherFormatA() {
        loadFXML("/fxml/ReviewTeacherFormatA.fxml", "Revisión Profesores Formato A");
    }

    public void showReviewCoordinatorFormatA() {
        loadFXML("/fxml/ReviewCoordinatorFormatA.fxml", "Revisión Coordinadores Formato A");
    }

    public void showPublishedTeacherFormatA() {
        loadFXML("/fxml/PublishedTeacherFormatA.fxml", "Formatos A Publicados");
    }

    public void showPersonalInformation() {
        loadFXML("/fxml/PersonalInformation.fxml", "Información Personal");
    }

    // Agrega este método a tu NavigationController
    public void showHomeWithUser(User usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            // Pasar el usuario al HomeController
            HomeController homeController = loader.getController();
            homeController.configurarConUsuario(usuario);
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Inicio - Workflow");
            primaryStage.show();
            
        } catch (IOException e) {
            throw new RuntimeException("Error cargando Home", e);
        }
    }
    //Método importante para cargar las vistas FXML
        private void loadFXML(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Error cargando: " + fxmlPath, e);
        }
    }
    
}