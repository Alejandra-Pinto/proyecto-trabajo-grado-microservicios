package com.unicauca.front.util;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.unicauca.front.controller.CoordinatorReviewFormatAController;
import com.unicauca.front.controller.HomeAdminController;
import com.unicauca.front.controller.HomeController;
import com.unicauca.front.controller.ManagementCoordinatorFormatAController;
import com.unicauca.front.controller.ManagementStudentDraftController;
import com.unicauca.front.controller.ManagementStudentFormatAController;
import com.unicauca.front.controller.ManagementTeacherDraftController;
import com.unicauca.front.controller.ManagementTeacherFormatAController;
import com.unicauca.front.controller.NotificationController;
import com.unicauca.front.controller.PersonalInformationController;
import com.unicauca.front.controller.PublishedDepartmentHeadDraftController;
import com.unicauca.front.controller.PublishedTeacherDraftController;
import com.unicauca.front.controller.PublishedTeacherFormatAController;
import com.unicauca.front.controller.StudentReviewFormatAController;
import com.unicauca.front.controller.TeacherReviewFormatAController;
import com.unicauca.front.model.DegreeWork;
import com.unicauca.front.model.User;

import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Component
public class NavigationController {
   private final ApplicationContext applicationContext;
   private Stage primaryStage;
   private HostServices hostServices;

   public NavigationController(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   public void setPrimaryStage(Stage primaryStage) {
      this.primaryStage = primaryStage;
   }

   public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    // Método para obtener HostServices
    public HostServices getHostServices() {
        return this.hostServices;
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

   public void showManagementCoordinatorFormatA(User usuario) {
    try {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ManagementCoordinatorFormatA.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        
        ManagementCoordinatorFormatAController controller = loader.getController();
        controller.configurarConUsuario(usuario);
        
        Scene scene = new Scene(root);
        this.primaryStage.setScene(scene);
        this.primaryStage.setTitle("Gestión Coordinadores Formato A");
        this.primaryStage.show();
    } catch (IOException e) {
        throw new RuntimeException("Error cargando ManagementCoordinatorFormatA", e);
    }
}

   public void showReviewStudentFormatA() {
      this.loadFXML("/fxml/ReviewStudentFormatA.fxml", "Revisión Estudiantes Formato A");
   }

   //Método para mostrar ReviewStudentFormatA con usuario y formato
   public void showStudentReviewFormatA(User usuario, DegreeWork formato) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ReviewStudentFormatA.fxml"));
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
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ReviewTeacherFormatA.fxml"));
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
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ReviewCoordinatorFormatA.fxml"));
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
   
    public void showPublishedTeacherDraft() {
        this.loadFXML("/fxml/PublishedTeacherDraft.fxml", "Anteproyectos Publicados");
    }

    public void showPublishedTeacherDraft(User usuario) {
       try {
          FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/PublishedTeacherDraft.fxml"));
          loader.setControllerFactory(applicationContext::getBean);
          Parent root = loader.load();

          PublishedTeacherDraftController controller = loader.getController();
          controller.configurarConUsuario(usuario);

          Scene scene = new Scene(root);
          this.primaryStage.setScene(scene);
          this.primaryStage.setTitle("Anteproyectos Publicados - Docente");
          this.primaryStage.show();
       } catch (IOException e) {
          throw new RuntimeException("Error cargando PublishedTeacherDraft", e);
       }
    }

   public void showPersonalInformation() {
      User usuarioActual = SessionManager.getCurrentUser();
      if (usuarioActual != null) {
         showPersonalInformation(usuarioActual);
      } else {
         this.loadFXML("/fxml/PersonalInformation.fxml", "Información Personal");
      }
   }

   public void showPersonalInformation(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/PersonalInformation.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         PersonalInformationController controller = loader.getController();
         //controller.configurarConUsuario(usuario);
         
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


   public void showManagementStudentDraft() {
      this.loadFXML("/fxml/ManagementStudentDraft.fxml", "Gestión Anteproyectos - Estudiante");
   }

   public void showManagementStudentDraft(User usuario) {
      try {
         FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ManagementStudentDraft.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         // Necesitarás crear este controller o ajustar el nombre según corresponda
         Object controller = loader.getController();
         if (controller instanceof ManagementStudentDraftController) {
               ((ManagementStudentDraftController) controller).configurarConUsuario(usuario);
         }
         
         Scene scene = new Scene(root);
         this.primaryStage.setScene(scene);
         this.primaryStage.setTitle("Gestión Anteproyectos - Estudiante");
         this.primaryStage.show();
      } catch (IOException e) {
         throw new RuntimeException("Error cargando ManagementStudentDraft", e);
      }
   }

   public void showManagementTeacherDraft() {
      this.loadFXML("/fxml/ManagementTeacherDraft.fxml", "Gestión Anteproyectos - Docente");
}

public void showManagementTeacherDraft(User usuario) {
   try {
      FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/ManagementTeacherDraft.fxml"));
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      
      // Necesitarás crear este controller o ajustar el nombre según corresponda
      Object controller = loader.getController();
      if (controller instanceof ManagementTeacherDraftController) {
            ((ManagementTeacherDraftController) controller).configurarConUsuario(usuario);
      }
      
      Scene scene = new Scene(root);
      this.primaryStage.setScene(scene);
      this.primaryStage.setTitle("Gestión Anteproyectos - Docente");
      this.primaryStage.show();
   } catch (IOException e) {
      throw new RuntimeException("Error cargando ManagementTeacherDraft", e);
   }
}

public void showManagementDepartmentHeadDraft() {
   this.loadFXML("/fxml/ManagementDepartmentHeadDraft.fxml", "Gestión Anteproyectos - Jefe Departamento");
}

public void showPublishedDepartmentHeadDraft() {
   this.loadFXML("/fxml/PublishedDepartmentHeadDraft.fxml", "Anteproyectos Publicados - Jefe Departamento");
}

public void showPublishedDepartmentHeadDraft(User usuario) {
   try {
      FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/PublishedDepartmentHeadDraft.fxml"));
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      
      // Necesitarás crear este controller o ajustar el nombre según corresponda
      Object controller = loader.getController();
      if (controller instanceof PublishedDepartmentHeadDraftController) {
            ((PublishedDepartmentHeadDraftController) controller).configurarConUsuario(usuario);
      }
      
      Scene scene = new Scene(root);
      this.primaryStage.setScene(scene);
      this.primaryStage.setTitle("Anteproyectos Publicados - Jefe Departamento");
      this.primaryStage.show();
   } catch (IOException e) {
      throw new RuntimeException("Error cargando PublishedDepartmentHeadDraft", e);
   }
}

   public void showNotificationPanel() {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Notification.fxml"));
         loader.setControllerFactory(applicationContext::getBean); 
         Parent root = loader.load();
         
         Stage stage = new Stage();
         stage.setTitle("Notificaciones - FIET");
         stage.setScene(new Scene(root));
         stage.initModality(Modality.APPLICATION_MODAL);
         stage.setOnHidden(e -> {
               NotificationController controller = loader.getController();
               controller.detenerActualizacion();
         });
         stage.showAndWait();
         
      } catch (Exception e) {
         e.printStackTrace();
         // Usa tu método mostrarAlerta existente o este alternativo:
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Error");
         alert.setHeaderText(null);
         alert.setContentText("No se pudo cargar el panel de notificaciones");
         alert.showAndWait();
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

   // Agrega este método en tu NavigationController
   public void showManagementEvaluadores() {
      try {
         System.out.println("DEBUG: Navegando a ManagementEvaluadores");
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManagementEvaluadores.fxml"));
         loader.setControllerFactory(applicationContext::getBean);
         Parent root = loader.load();
         
         Scene scene = new Scene(root);
         primaryStage.setScene(scene);
         primaryStage.setTitle("Gestión de Evaluadores");
         primaryStage.show();
      } catch (Exception e) {
         e.printStackTrace();
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Error");
         alert.setHeaderText("No se pudo cargar la gestión de evaluadores");
         alert.setContentText(e.getMessage());
         alert.showAndWait();
      }
   }

   public void logout() {
      SessionManager.clearSession();
      showLogin();
      System.out.println("✅ Sesión cerrada correctamente");
   }
}