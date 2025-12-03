package com.unicauca.front;

import com.unicauca.front.service.ApiGatewayService;
import com.unicauca.front.util.NavigationController;
import com.unicauca.front.util.SessionManager;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FrontApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private NavigationController navigationController;
    private static HostServices appHostServices;

    @Override
    public void init() {
        this.springContext = new SpringApplicationBuilder()
                .sources(FrontApplication.class)
                .run();
        this.navigationController = springContext.getBean(NavigationController.class);
    }

    @Override
    public void start(Stage primaryStage) {
        // Guardar HostServices para uso global
        appHostServices = getHostServices();
        
        // Configurar HostServices en NavigationController
        navigationController.setHostServices(appHostServices);
        
        navigationController.setPrimaryStage(primaryStage);
        navigationController.showLogin();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    @Bean
    public CommandLineRunner setupSessionManager(ApiGatewayService apiGatewayService) {
        return args -> {
            SessionManager.setApiGatewayService(apiGatewayService);
            System.out.println("✅ SessionManager configurado con ApiGatewayService");
        };
    }

    // Método estático para acceder a HostServices desde cualquier lugar
    public static HostServices getAppHostServices() {
        return appHostServices;
    }

    public static void main(String[] args) {
        launch(args);
    }
}