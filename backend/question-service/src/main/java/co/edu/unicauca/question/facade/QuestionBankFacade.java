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
import co.edu.unicauca.question.dto.PageDTO;
import co.edu.unicauca.question.dto.PresentationDTO;
import co.edu.unicauca.question.dto.QuestionFilter;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.dto.QuestionResponseDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.dto.SubmitRequestDTO;
import co.edu.unicauca.question.dto.OperationDTO;
import co.edu.unicauca.question.exception.ForbiddenException;
import co.edu.unicauca.question.exception.ValidationFailedException;
import co.edu.unicauca.question.messaging.SubmissionNotifier;
import co.edu.unicauca.question.repository.QuestionRepository;
import co.edu.unicauca.question.security.AuthenticatedUser;
import co.edu.unicauca.question.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUser;
    private final SubmissionNotifier submissionNotifier;
    private final QuestionProvider externalProvider;

    @Autowired
    public QuestionBankFacade(QuestionService questionService, QuestionRepository questionRepository,
                              CurrentUserProvider currentUser, SubmissionNotifier submissionNotifier) {
        this(questionService, questionRepository, currentUser, submissionNotifier,
                new ExternalQuestionAdapter(new ExternalQuestionService()));
    }

    public QuestionBankFacade(QuestionService questionService, QuestionRepository questionRepository,
                              CurrentUserProvider currentUser, SubmissionNotifier submissionNotifier,
                              QuestionProvider externalProvider) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
        this.currentUser = currentUser;
        this.submissionNotifier = submissionNotifier;
        this.externalProvider = externalProvider;
    }

    /** El autor siempre es el usuario del JWT; un authorId distinto en el cuerpo se rechaza con 403. */
    public QuestionResponseDTO createQuestion(QuestionRequestDTO request) {
        AuthenticatedUser me = currentUser.current();
        return questionService.create(request.withAuthorId(me.resolveId(request.authorId(), "authorId")));
    }

    /** Autores: solo sus preguntas. Administradores y revisores: todas. */
    public List<QuestionResponseDTO> listQuestions() {
        AuthenticatedUser me = currentUser.current();
        return me.canSeeAll() ? questionService.list() : questionService.listByAuthor(me.id());
    }

    /** Listado paginado y filtrado; un autor solo puede consultar sus propias preguntas. */
    public PageDTO<QuestionResponseDTO> searchQuestions(QuestionFilter filter, int page, int size) {
        AuthenticatedUser me = currentUser.current();
        QuestionFilter scoped = filter;
        if (!me.canSeeAll()) {
            scoped = filter.withAuthorId(me.resolveId(filter.authorId(), "authorId"));
        }
        return questionService.search(scoped, page, size);
    }

    public QuestionResponseDTO findQuestion(Long id) {
        AuthenticatedUser me = currentUser.current();
        QuestionResponseDTO question = questionService.get(id);
        if (!me.canSeeAll() && !me.id().equals(question.authorId())) {
            throw new ForbiddenException("Solo el autor de la pregunta puede consultarla");
        }
        return question;
    }

    public QuestionResponseDTO updateQuestion(Long id, QuestionRequestDTO request) {
        AuthenticatedUser me = currentUser.current();
        return questionService.update(id, request.withAuthorId(me.resolveId(request.authorId(), "authorId")));
    }

    /**
     * Solo el autor envía su pregunta a revisión. Si la solicitud corresponde a un borrador o a una pregunta
     * rechazada corregida, avisa a los administradores para que asignen revisores.
     */
    public SubmitAcceptedDTO submitForReview(Long questionId, SubmitRequestDTO body) {
        AuthenticatedUser me = currentUser.current();
        Long requestedBy = me.resolveId(body == null ? null : body.requestedBy(), "requestedBy");
        QuestionResponseDTO question = questionService.get(questionId);
        if (!me.id().equals(question.authorId())) {
            throw new ForbiddenException("Solo el autor de la pregunta puede enviarla a revisión");
        }
        SubmitAcceptedDTO accepted = questionService.submitForReview(questionId, requestedBy);
        boolean resubmission = "REJECTED".equals(question.status());
        if (resubmission || "DRAFT".equals(question.status())) {
            submissionNotifier.notifyAdministrators(me, questionId, resubmission);
        }
        return accepted;
    }

    public OperationDTO getOperation(String operationId) { return questionService.getOperation(operationId); }

    public ImportReport importExternalQuestions(Long requestedAuthorId) {
        AuthenticatedUser me = currentUser.current();
        Long authorId = me.resolveId(requestedAuthorId, "authorId");
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
        QuestionResponseDTO dto = findQuestion(id);
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
