package co.edu.unicauca.validation.controller;

import co.edu.unicauca.validation.dto.EvaluateRequestDTO;
import co.edu.unicauca.validation.dto.EvaluateResponseDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import co.edu.unicauca.validation.service.EvaluationService;
import co.edu.unicauca.validation.service.ValidationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ValidationController {

    private final ValidationService validationService;
    private final EvaluationService evaluationService;

    public ValidationController(ValidationService validationService, EvaluationService evaluationService) {
        this.validationService = validationService;
        this.evaluationService = evaluationService;
    }

    @PostMapping("/validate")
    public ValidationResultDTO validate(@RequestBody ValidationRequestDTO request) {
        return validationService.validate(request);
    }

    /** Patrón Template Method: evalúa una respuesta según el tipo de pregunta. */
    @PostMapping("/evaluate")
    public EvaluateResponseDTO evaluate(@RequestBody EvaluateRequestDTO request) {
        return evaluationService.evaluate(request);
    }
}
