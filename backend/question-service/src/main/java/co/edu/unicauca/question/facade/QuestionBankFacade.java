package co.edu.unicauca.question.facade;

import co.edu.unicauca.question.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.question.adapter.ExternalQuestionService;
import co.edu.unicauca.question.adapter.QuestionProvider;
import co.edu.unicauca.question.decorator.BasicQuestion;
import co.edu.unicauca.question.decorator.FeedbackQuestion;
import co.edu.unicauca.question.decorator.HighDifficultyQuestion;
import co.edu.unicauca.question.decorator.QuestionComponent;
import co.edu.unicauca.question.domain.model.Context;
import co.edu.unicauca.question.domain.model.Option;
import co.edu.unicauca.question.domain.model.Question;
import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.PresentationDTO;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.dto.QuestionResponseDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.dto.SubmitRequestDTO;
import co.edu.unicauca.question.dto.OperationDTO;
import co.edu.unicauca.question.exception.ForbiddenException;
import co.edu.unicauca.question.exception.ValidationFailedException;
import co.edu.unicauca.question.repository.QuestionRepository;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Patrón Facade: interfaz única y simple para la capa de presentación (controller).
 */
@Service
public class QuestionBankFacade {
    public record ImportReport(int imported, int duplicates, int rejected, List<String> messages) {}

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final QuestionProvider externalProvider;

    @Autowired
    public QuestionBankFacade(QuestionService questionService, QuestionRepository questionRepository) {
        this(questionService, questionRepository, new ExternalQuestionAdapter(new ExternalQuestionService()));
    }

    public QuestionBankFacade(QuestionService questionService, QuestionRepository questionRepository,
                              QuestionProvider externalProvider) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
        this.externalProvider = externalProvider;
    }

    public QuestionResponseDTO createQuestion(QuestionRequestDTO request) {
        return questionService.create(request);
    }

    public List<QuestionResponseDTO> listQuestions() { return questionService.list(); }
    public QuestionResponseDTO findQuestion(Long id) { return questionService.get(id); }
    public QuestionResponseDTO updateQuestion(Long id, QuestionRequestDTO request) {
        return questionService.update(id, request);
    }

    public SubmitAcceptedDTO submitForReview(Long questionId, SubmitRequestDTO body) {
        return questionService.submitForReview(questionId, body == null ? null : body.requestedBy());
    }

    public OperationDTO getOperation(String operationId) { return questionService.getOperation(operationId); }

    public ImportReport importExternalQuestions(Long authorId) {
        List<String> messages = new ArrayList<>();
        int imported = 0, duplicates = 0, rejected = 0;
        for (Question q : externalProvider.getQuestions()) {
            String code = q.getId();
            if (questionRepository.existsByExternalCode(code)) {
                duplicates++;
                messages.add(code + ": duplicada, omitida");
                continue;
            }
            try {
                QuestionResponseDTO created = questionService.create(toRequest(q, authorId));
                questionRepository.findById(created.id()).ifPresent(entity -> {
                    entity.setExternalCode(code);
                    questionRepository.save(entity);
                });
                imported++;
                messages.add(code + ": importada como " + created.id());
            } catch (ValidationFailedException e) {
                rejected++;
                messages.add(code + ": rechazada (" + String.join("; ", e.getErrors()) + ")");
            } catch (ForbiddenException e) {
                messages.add("Importación detenida: " + e.getMessage());
                break;
            }
        }
        return new ImportReport(imported, duplicates, rejected, messages);
    }

    public PresentationDTO presentation(Long id, boolean altaDificultad, String retroalimentacion) {
        QuestionResponseDTO dto = questionService.get(id);
        QuestionComponent component = new BasicQuestion(toDomain(dto));
        if (altaDificultad) {
            component = new HighDifficultyQuestion(component);
        }
        if (retroalimentacion != null && !retroalimentacion.isBlank()) {
            component = new FeedbackQuestion(component, retroalimentacion);
        }
        return new PresentationDTO(component.getDescription(), component.getExplanation(), component.getMaxScore());
    }

    private QuestionRequestDTO toRequest(Question q, Long authorId) {
        List<OptionDTO> options = q.getOptions().stream()
                .map(o -> new OptionDTO(o.getLabel(), o.getText()))
                .toList();
        String correct = q.getCorrectOption().map(Option::getLabel).orElse("");
        return new QuestionRequestDTO(authorId, q.getContext().getText(), q.getStatement(), options, correct,
                q.getJustification(), q.getCompetency(), q.getTopic(), q.getSubtopic(),
                "Fuente externa: " + q.getId(), "MEDIUM");
    }

    private Question toDomain(QuestionResponseDTO dto) {
        String correct = dto.correctOption() == null ? "" : dto.correctOption().trim().toUpperCase();
        List<Option> options = dto.options() == null ? List.of() : dto.options().stream()
                .sorted(Comparator.comparing(o -> o.label() == null ? "" : o.label()))
                .map(o -> new Option(o.label() == null ? "" : o.label().trim().toUpperCase(), o.text(),
                        o.label() != null && o.label().trim().equalsIgnoreCase(correct)))
                .toList();
        return new Question(String.valueOf(dto.id()), new Context(dto.context()), dto.statement(), options,
                dto.justification(), dto.competency(), dto.topic(), dto.subtopic());
    }
}
