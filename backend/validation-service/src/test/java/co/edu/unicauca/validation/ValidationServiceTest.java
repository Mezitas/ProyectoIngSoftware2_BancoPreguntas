package co.edu.unicauca.validation;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.service.ValidationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationServiceTest {

    private final ValidationService service = new ValidationService();

    private static List<OptionDTO> fourOptions() {
        return List.of(new OptionDTO("A", "uno"), new OptionDTO("B", "dos"),
                new OptionDTO("C", "tres"), new OptionDTO("D", "cuatro"));
    }

    private static ValidationRequestDTO valid() {
        return new ValidationRequestDTO("contexto", "enunciado", fourOptions(), "B",
                "justificación", "Razonamiento cuantitativo", "Álgebra", "Ecuaciones",
                "Texto de referencia", "MEDIUM");
    }

    @Test
    void preguntaCompletaEsValida() {
        ValidationResultDTO r = service.validate(valid());
        assertTrue(r.valid());
        assertTrue(r.errors().isEmpty());
    }

    @Test
    void conTresOpcionesEsInvalida() {
        var q = new ValidationRequestDTO("c", "e", fourOptions().subList(0, 3), "B", "j", "k", "t", "s",
                "referencia", "MEDIUM");
        ValidationResultDTO r = service.validate(q);
        assertFalse(r.valid());
        assertTrue(r.errors().stream().anyMatch(e -> e.contains("exactamente 4 opciones")));
    }

    @Test
    void sinRespuestaCorrectaEsInvalida() {
        var q = new ValidationRequestDTO("c", "e", fourOptions(), null, "j", "k", "t", "s",
                "referencia", "MEDIUM");
        assertFalse(service.validate(q).valid());
    }

    @Test
    void respuestaCorrectaInexistenteEsInvalida() {
        var q = new ValidationRequestDTO("c", "e", fourOptions(), "Z", "j", "k", "t", "s",
                "referencia", "MEDIUM");
        ValidationResultDTO r = service.validate(q);
        assertTrue(r.errors().stream().anyMatch(e -> e.contains("no corresponde a ninguna opción")));
    }

    @Test
    void sinEnunciadoEsInvalida() {
        var q = new ValidationRequestDTO("c", " ", fourOptions(), "A", "j", "k", "t", "s",
                "referencia", "MEDIUM");
        assertTrue(service.validate(q).errors().contains("la pregunta debe tener enunciado"));
    }

    @Test
    void opcionesRepetidasSonInvalidas() {
        var opts = List.of(new OptionDTO("A", "igual"), new OptionDTO("B", "igual"),
                new OptionDTO("C", "tres"), new OptionDTO("D", "cuatro"));
        var q = new ValidationRequestDTO("c", "e", opts, "A", "j", "k", "t", "s",
                "referencia", "MEDIUM");
        assertTrue(service.validate(q).errors().contains("hay opciones repetidas"));
    }

    @Test
    void acumulaTodosLosErrores() {
        var q = new ValidationRequestDTO(null, null, null, null, null, null, null, null, null, null);
        assertTrue(service.validate(q).errors().size() >= 5);
    }

    @Test
    void sinBibliografiaEsInvalida() {
        var q = new ValidationRequestDTO("c", "e", fourOptions(), "A", "j", "k", "t", "s", null, "MEDIUM");
        assertTrue(service.validate(q).errors().contains("falta la bibliografía"));
    }

    @Test
    void nivelDificultadDesconocidoEsInvalido() {
        var q = new ValidationRequestDTO("c", "e", fourOptions(), "A", "j", "k", "t", "s", "referencia", "EXPERT");
        assertTrue(service.validate(q).errors().stream().anyMatch(e -> e.contains("nivel de dificultad")));
    }
}
