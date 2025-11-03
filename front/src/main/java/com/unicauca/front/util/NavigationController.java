package com.unicauca.front.util;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.unicauca.front.controller.CoordinatorReviewFormatAController;
import com.unicauca.front.controller.HomeAdminController;
import com.unicauca.front.controller.HomeController;
import com.unicauca.front.controller.ManagementStudentFormatAController;
import com.unicauca.front.controller.ManagementTeacherFormatAController;
import com.unicauca.front.controller.PersonalInformationController;
import com.unicauca.front.controller.PublishedTeacherFormatAController;
import com.unicauca.front.controller.StudentReviewFormatAController;
import com.unicauca.front.controller.TeacherReviewFormatAController;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

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

   public void showLogin() {
      this.loadFXML("/fxml/Login.fxml", "Iniciar Sesión");
   }

   public void showRegister() {
      this.loadFXML("/fxml/Register.fxml", "Registrar Usuario");
   }

   public void showHomeAdmin() {
      this.loadFXML("/fxml/HomeAdmin.fxml", "Panel de Administración");
   }

   public void showHomeAdmin(User usuario) {
      System.out.println("showHomeAdmin invocado con usuario: " + usuario);

    try {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/HomeAdmin.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        
        HomeAdminController controller = loader.getController();
        controller.configurarConUsuario(usuario);
        
        Scene scene = new Scene(root);
        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("Panel de Administración");
        this.primaryStage.show();
    } catch (IOException e) {
      e.printStackTrace();
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error al cargar interfaz");
      alert.setHeaderText("No se pudo cargar HomeAdmin.fxml");
      alert.setContentText(e.getMessage());
      alert.showAndWait();
   }

}

   public void showHome() {
      this.loadFXML("/fxml/Home.fxml", "Inicio");
   }

   public void showManagementAdmin() {
      this.loadFXML("/fxml/ManagementAdmin.fxml", "Gestión Administradores");
   }

   public void showManagementStudentFormatA() {
      this.loadFXML("/fxml/ManagementStudentFormatA.fxml", "Gestión Estudiantes Formato A");
   }

   //Método para mostrar ManagementStudentFormatA con usuario
   public void showManagementStudentFormatA(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ManagementStudentFormatA.fxml"));
         ApplicationContext var10001 = this.applicationContext;
         loader.setControllerFactory(var10001::getBean);
         Parent root = (Parent)loader.load();
         ManagementStudentFormatAController controller = (ManagementStudentFormatAController)loader.getController();
         controller.configurarConUsuario(usuario);
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Gestión Estudiantes Formato A");
         this.primaryStage.show();
      } catch (IOException var7) {
         throw new RuntimeException("Error cargando ManagementStudentFormatA", var7);
      }
   }

   public void showManagementTeacherFormatA() {
      this.loadFXML("/fxml/ManagementTeacherFormatA.fxml", "Gestión Profesores Formato A");
   }

   public void showManagementTeacherFormatAWithFormato(User usuario, DegreeWork formato) {
    try {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ManagementTeacherFormatA.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        
        ManagementTeacherFormatAController controller = loader.getController();
        controller.configurarConUsuario(usuario);
        controller.configurarConFormato(formato);
        controller.deshabilitarCamposFijos(); // Para re-subir
        
        Scene scene = new Scene(root);
        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("Re-subir Formato A - Docente");
        this.primaryStage.show();
    } catch (IOException e) {
        throw new RuntimeException("Error cargando ManagementTeacherFormatA con formato", e);
    }
}

   public void showManagementCoordinatorFormatA() {
      this.loadFXML("/fxml/ManagementCoordinatorFormatA.fxml", "Gestión Coordinadores Formato A");
   }

   public void showReviewStudentFormatA() {
      this.loadFXML("/fxml/ReviewStudentFormatA.fxml", "Revisión Estudiantes Formato A");
   }

   //Método para mostrar ReviewStudentFormatA con usuario y formato
   public void showStudentReviewFormatA(User usuario, DegreeWork formato) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/StudentReviewFormatA.fxml"));
         ApplicationContext var10001 = this.applicationContext;
         loader.setControllerFactory(var10001::getBean);
         Parent root = (Parent)loader.load();
         StudentReviewFormatAController controller = (StudentReviewFormatAController)loader.getController();
         controller.setUsuarioYFormato(usuario, formato);
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Revisión de Correcciones - Formato A");
         this.primaryStage.show();
      } catch (IOException var7) {
         throw new RuntimeException("Error cargando StudentReviewFormatA", var7);
      }
   }

   public void showTeacherReviewFormatA(User usuario, DegreeWork formato) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/TeacherReviewFormatA.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         TeacherReviewFormatAController controller = loader.getController();
         controller.setUsuarioYFormato(usuario, formato);
         
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Revisión de Correcciones - Docente");
         this.primaryStage.show();
      } catch (IOException e) {
         throw new RuntimeException("Error cargando TeacherReviewFormatA", e);
      }
   }

   public void showCoordinatorReviewFormatA(User usuario, DegreeWork formato) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/CoordinatorReviewFormatA.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         CoordinatorReviewFormatAController controller = loader.getController();
         controller.setFormato(formato);     
         controller.configurarConUsuario(usuario); 
         
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Revisión de Formato - Coordinador");
         this.primaryStage.show();
      } catch (IOException e) {
         throw new RuntimeException("Error cargando CoordinatorReviewFormatA", e);
      }
   }

   public void showPublishedTeacherFormatA() {
      this.loadFXML("/fxml/PublishedTeacherFormatA.fxml", "Formatos A Publicados");
   }

   public void showPublishedTeacherFormatA(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/PublishedTeacherFormatA.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         PublishedTeacherFormatAController controller = loader.getController();
         controller.configurarConUsuario(usuario);
         
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Formatos Publicados - Docente");
         this.primaryStage.show();
      } catch (IOException e) {
         throw new RuntimeException("Error cargando PublishedTeacherFormatA", e);
      }
   }

   public void showPersonalInformation() {
      this.loadFXML("/fxml/PersonalInformation.fxml", "Información Personal");
   }

   public void showPersonalInformation(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/PersonalInformation.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         PersonalInformationController controller = loader.getController();
         controller.configurarConUsuario(usuario);
         
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Información Personal");
         this.primaryStage.show();
      } catch (IOException e) {
         throw new RuntimeException("Error cargando PersonalInformation", e);
      }
   }

   public void showHomeWithUser(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/Home.fxml"));
         ApplicationContext var10001 = this.applicationContext;
         loader.setControllerFactory(var10001::getBean);
         Parent root = (Parent)loader.load();
         HomeController homeController = (HomeController)loader.getController();
         homeController.configurarConUsuario(usuario);
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Inicio - Workflow");
         this.primaryStage.show();
      } catch (IOException var7) {
         throw new RuntimeException("Error cargando Home", var7);
      }
   }

   public void showStatistics() {
    loadFXML("/fxml/Statistics.fxml", "Estadísticas de Formatos");
   }

   private void loadFXML(String fxmlPath, String title) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fxmlPath));
         ApplicationContext var10001 = this.applicationContext;
         loader.setControllerFactory(var10001::getBean);
         Parent root = (Parent)loader.load();
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle(title);
         this.primaryStage.show();
      } catch (IOException var7) {
         throw new RuntimeException("Error cargando: " + fxmlPath, var7);
      }
   }
}