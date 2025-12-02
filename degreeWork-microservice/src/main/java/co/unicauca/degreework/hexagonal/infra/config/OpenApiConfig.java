package co.unicauca.degreework.hexagonal.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Degree Work API")
                    .version("1.0.0")
                    .description("API for managing degree works at Unicauca")
                    .termsOfService("http://swagger.io/terms")
                    .contact(new Contact()
                        .name("Sebastian Caicedo Castro")
                        .email("jhoansecaicedo@unicauca.edu.co"))
                    .extensions(Map.of(
                        "contact2", Map.of(
                            "name", "Alejandra Pinto Beltran",
                            "email", "alejandrapinto@unicauca.edu.co"
                        ),
                        "contact3", Map.of(
                            "name", "Dana Isabella Romero Nuñez",
                            "email", "danaromero@unicauca.edu.co"
                        ),
                        "contact4", Map.of(
                            "name", "María Juliana Sánchez Galvis",
                            "email", "mariajulisanchez@unicauca.edu.co"
                        )
                    )))
                .servers(List.of(
                    new Server()
                        .url("http://localhost:8081")
                        .description("Submission Service HTTP")
                ));
    }
}
