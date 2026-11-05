package co.edu.unicauca.review.controller;

import co.edu.unicauca.review.dto.*;
import co.edu.unicauca.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    /** 201 si la asignación quedó confirmada (pregunta en IN_REVIEW) y el evento fue encolado. */
    @PostMapping("/assign")
    public ResponseEntity<AssignmentDTO> assign(@Valid @RequestBody AssignRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assign(request));
    }

    /** Registra el voto del revisor; Lifecycle finaliza cuando todos los revisores asignados responden. */
    @PostMapping("/result")
    public ReviewDTO result(@Valid @RequestBody ReviewResultRequestDTO request) {
        return service.submitResult(request);
    }

    @GetMapping("/question/{questionId}")
    public List<AssignmentDTO> byQuestion(@PathVariable Long questionId) {
        return service.findByQuestion(questionId);
    }

    @GetMapping("/question/{questionId}/results")
    public List<ReviewDTO> resultsByQuestion(@PathVariable Long questionId) {
        return service.findResultsByQuestion(questionId);
    }

    @GetMapping("/reviewer/{reviewerId}")
    public List<AssignmentDTO> byReviewer(@PathVariable Long reviewerId) {
        return service.findByReviewer(reviewerId);
    }

    @GetMapping("/assignments/{id}")
    public AssignmentDTO assignment(@PathVariable Long id) {
        return service.findAssignment(id);
    }
}
