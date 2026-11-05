package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.PresentationDTO;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.dto.QuestionResponseDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.dto.SubmitRequestDTO;
import co.edu.unicauca.question.facade.QuestionBankFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionBankFacade facade;

    public QuestionController(QuestionBankFacade facade) { this.facade = facade; }

    @PostMapping
    public ResponseEntity<QuestionResponseDTO> create(@Valid @RequestBody QuestionRequestDTO request) {
        QuestionResponseDTO created = facade.createQuestion(request);
        return ResponseEntity.created(URI.create("/questions/" + created.id())).body(created);
    }

    @GetMapping
    public List<QuestionResponseDTO> list() { return facade.listQuestions(); }

    @GetMapping("/{id}")
    public QuestionResponseDTO get(@PathVariable Long id) { return facade.findQuestion(id); }

    @PutMapping("/{id}")
    public QuestionResponseDTO update(@PathVariable Long id, @Valid @RequestBody QuestionRequestDTO request) {
        return facade.updateQuestion(id, request);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitAcceptedDTO> submit(@PathVariable Long id,
                                                    @RequestBody(required = false) SubmitRequestDTO body) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(facade.submitForReview(id, body));
    }

    /** Patrón Adapter + Facade: importa los reactivos de la plataforma externa. */
    @PostMapping("/import")
    public QuestionBankFacade.ImportReport importExternal(@RequestParam Long authorId) {
        return facade.importExternalQuestions(authorId);
    }

    /** Patrón Decorator: presentación enriquecida (alta dificultad y/o retroalimentación técnica). */
    @GetMapping("/{id}/presentation")
    public PresentationDTO presentation(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean altaDificultad,
            @RequestParam(required = false) String retroalimentacion) {
        return facade.presentation(id, altaDificultad, retroalimentacion);
    }
}
