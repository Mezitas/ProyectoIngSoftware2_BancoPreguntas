package co.edu.unicauca.user.config;

import co.edu.unicauca.user.model.UserEntity;
import co.edu.unicauca.user.repository.UserRepository;
import co.edu.unicauca.user.service.PasswordHasher;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Usuarios de prueba (ids 1..5 en una base limpia). */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(new UserEntity("docente1", PasswordHasher.hash("Docente123*"),
                    "Ana Docente", "docente1@unicauca.edu.co", Set.of("DOCENTE", "AUTOR")));
            repo.save(new UserEntity("admin1", PasswordHasher.hash("Admin123*"),
                    "Carlos Admin", "admin1@unicauca.edu.co", Set.of("ADMIN")));
            repo.save(new UserEntity("revisor1", PasswordHasher.hash("Revisor123*"),
                    "Rosa Revisora", "revisor1@unicauca.edu.co", Set.of("REVISOR")));
            repo.save(new UserEntity("revisor2", PasswordHasher.hash("Revisor123*"),
                    "Rafael Revisor", "revisor2@unicauca.edu.co", Set.of("REVISOR")));
            repo.save(new UserEntity("estudiante1", PasswordHasher.hash("Estudiante123*"),
                    "Elena Estudiante", "estudiante1@unicauca.edu.co", Set.of("ESTUDIANTE")));
        };
    }
}
