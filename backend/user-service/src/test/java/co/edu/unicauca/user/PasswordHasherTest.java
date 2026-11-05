package co.edu.unicauca.user;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unicauca.user.service.PasswordHasher;
import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void laContrasenaCorrectaCoincideConSuHash() {
        String hash = PasswordHasher.hash("Docente123*");
        assertTrue(PasswordHasher.matches("Docente123*", hash));
    }

    @Test
    void unaContrasenaIncorrectaNoCoincide() {
        String hash = PasswordHasher.hash("Docente123*");
        assertFalse(PasswordHasher.matches("otra", hash));
    }

    @Test
    void elHashNoEsLaContrasenaEnClaro() {
        assertNotEquals("Docente123*", PasswordHasher.hash("Docente123*"));
    }
}
