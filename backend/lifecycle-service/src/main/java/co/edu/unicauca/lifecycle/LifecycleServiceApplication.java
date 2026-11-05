package co.edu.unicauca.lifecycle;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Lifecycle Service API",
        version = "1.0.0",
        description = "Consulta y transición del ciclo de vida de las preguntas."
))
public class LifecycleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifecycleServiceApplication.class, args);
    }
}
