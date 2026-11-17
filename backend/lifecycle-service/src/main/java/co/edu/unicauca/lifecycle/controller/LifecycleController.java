package co.edu.unicauca.lifecycle.controller;

import co.edu.unicauca.lifecycle.dto.*;
import co.edu.unicauca.lifecycle.security.TransitionPolicy;
import co.edu.unicauca.lifecycle.service.LifecycleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {

    private final LifecycleService service;

    public LifecycleController(LifecycleService service) {
        this.service = service;
    }

    /** Registra una pregunta nueva en DRAFT (la invoca Question Service tras persistirla). */
    @PostMapping
    public ResponseEntity<StateDTO> register(@Valid @RequestBody RegisterQuestionDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request.questionId(), request.createdBy()));
    }

    @GetMapping("/{questionId}")
    public StateDTO get(@PathVariable Long questionId) {
        return service.get(questionId);
    }

    @GetMapping("/{questionId}/history")
    public List<HistoryEntryDTO> history(@PathVariable Long questionId) {
        return service.history(questionId);
    }

    /** Transición síncrona solicitada por otro servicio (p. ej. Review Service: PENDING_REVIEW -> IN_REVIEW). */
    @PostMapping("/{questionId}/transition")
    public TransitionResultDTO transition(@PathVariable Long questionId,
                                          @Valid @RequestBody TransitionRequestDTO request,
                                          Authentication authentication) {
        TransitionPolicy.check(String.valueOf(request.targetState()), rolesOf(authentication));
        return service.transition(questionId, request.targetState(), request.requestedBy(), request.comment(), null);
    }

    private static List<String> rolesOf(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();
    }

    /** Resultado de una operación asíncrona (comando procesado desde RabbitMQ). */
    @GetMapping("/operations/{operationId}")
    public OperationDTO operation(@PathVariable String operationId) {
        return service.getOperation(operationId);
    }
}
