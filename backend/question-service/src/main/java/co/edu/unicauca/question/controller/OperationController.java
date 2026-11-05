package co.edu.unicauca.question.controller;

import co.edu.unicauca.question.dto.OperationDTO;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private final QuestionService service;

    public OperationController(QuestionService service) {
        this.service = service;
    }

    /** Consulta el resultado de una operación asíncrona: PENDING, COMPLETED o FAILED. */
    @GetMapping("/{operationId}")
    public OperationDTO get(@PathVariable String operationId) {
        return service.getOperation(operationId);
    }
}
