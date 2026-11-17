package co.edu.unicauca.review.controller;

import co.edu.unicauca.review.dto.*;
import co.edu.unicauca.review.security.AuthenticatedUser;
import co.edu.unicauca.review.security.CurrentUserProvider;
import co.edu.unicauca.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * La identidad de quien asigna, evalúa o consulta sale siempre del JWT: los ids que lleguen en el cuerpo o en la
 * ruta solo se aceptan si coinciden con el usuario autenticado (o si es administrador, en las consultas).
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;
    private final CurrentUserProvider currentUser;

    public ReviewController(ReviewService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    /** 201 si la asignación quedó confirmada (pregunta en IN_REVIEW) y el evento fue encolado. */
    @PostMapping("/assign")
    public ResponseEntity<AssignmentDTO> assign(@Valid @RequestBody AssignRequestDTO request) {
        AuthenticatedUser me = currentUser.current();
        AssignRequestDTO trusted = request.withAssignedBy(me.resolveId(request.assignedBy(), "assignedBy"));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assign(trusted));
    }

    /** Registra el voto del revisor; Lifecycle finaliza cuando todos los revisores asignados responden. */
    @PostMapping("/result")
    public ReviewDTO result(@Valid @RequestBody ReviewResultRequestDTO request) {
        AuthenticatedUser me = currentUser.current();
        return service.submitResult(request.withReviewerId(me.resolveId(request.reviewerId(), "reviewerId")));
    }

    @GetMapping("/question/{questionId}")
    public List<AssignmentDTO> byQuestion(@PathVariable Long questionId) {
        service.assertCanView(questionId, currentUser.current());
        return service.findByQuestion(questionId);
    }

    /** Historial de evaluaciones (todas las rondas) con sus observaciones, para que el autor corrija. */
    @GetMapping("/question/{questionId}/results")
    public List<ReviewDTO> resultsByQuestion(@PathVariable Long questionId) {
        service.assertCanView(questionId, currentUser.current());
        return service.findResultsByQuestion(questionId);
    }

    /** Preguntas asignadas a un revisor: cada revisor ve las suyas; el administrador puede ver las de cualquiera. */
    @GetMapping("/reviewer/{reviewerId}")
    public List<AssignmentDTO> byReviewer(@PathVariable Long reviewerId) {
        AuthenticatedUser me = currentUser.current();
        if (!me.hasRole("ADMIN")) {
            me.resolveId(reviewerId, "reviewerId");
        }
        return service.findByReviewer(reviewerId);
    }

    @GetMapping("/assignments/{id}")
    public AssignmentDTO assignment(@PathVariable Long id) {
        AuthenticatedUser me = currentUser.current();
        AssignmentDTO assignment = service.findAssignment(id);
        service.assertCanView(assignment.questionId(), me);
        return assignment;
    }
}
