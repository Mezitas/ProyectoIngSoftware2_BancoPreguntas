package co.edu.unicauca.question.builder;

import co.edu.unicauca.question.domain.model.Context;
import co.edu.unicauca.question.domain.model.Option;
import co.edu.unicauca.question.domain.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Patrón Builder - constructor concreto. Acumula las partes y, en {@link #build()},
 * valida las reglas del reactivo Saber PRO (4 opciones, 1 correcta, enunciado).
 * Después de construir queda limpio para armar otra pregunta.
 */
public class QuestionBuilder implements IQuestionBuilder {
    public static final int REQUIRED_OPTIONS = 4;
    private static final String LABELS = "ABCD";

    private String id;
    private String context;
    private String statement;
    private final List<String> optionTexts = new ArrayList<>();
    private final List<Boolean> optionCorrect = new ArrayList<>();
    private String justification;
    private String competency;
    private String topic;
    private String subtopic;

    public QuestionBuilder() { reset(); }

    private void reset() {
        id = null; context = ""; statement = "";
        optionTexts.clear(); optionCorrect.clear();
        justification = ""; competency = ""; topic = ""; subtopic = "";
    }

    @Override public IQuestionBuilder setId(String id) { this.id = id; return this; }
    @Override public IQuestionBuilder setContext(String context) { this.context = context; return this; }
    @Override public IQuestionBuilder setStatement(String statement) { this.statement = statement; return this; }
    @Override public IQuestionBuilder addOption(String text, boolean correct) {
        optionTexts.add(text); optionCorrect.add(correct); return this;
    }
    @Override public IQuestionBuilder setJustification(String justification) {
        this.justification = justification; return this;
    }
    @Override public IQuestionBuilder setClassification(String competency, String topic, String subtopic) {
        this.competency = competency; this.topic = topic; this.subtopic = subtopic; return this;
    }

    @Override
    public Question build() {
        validate();
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            options.add(new Option(String.valueOf(LABELS.charAt(i)), optionTexts.get(i), optionCorrect.get(i)));
        }
        Question question = new Question(
                id == null || id.isBlank() ? UUID.randomUUID().toString() : id,
                new Context(context),
                statement.trim(),
                options,
                justification == null ? "" : justification.trim(),
                competency == null ? "" : competency.trim(),
                topic == null ? "" : topic.trim(),
                subtopic == null ? "" : subtopic.trim());
        reset();
        return question;
    }

    private void validate() {
        List<String> errors = new ArrayList<>();
        if (statement == null || statement.isBlank()) {
            errors.add("la pregunta debe tener enunciado");
        }
        if (optionTexts.size() != REQUIRED_OPTIONS) {
            errors.add("debe tener exactamente " + REQUIRED_OPTIONS + " opciones (tiene " + optionTexts.size() + ")");
        }
        long correctCount = optionCorrect.stream().filter(Boolean::booleanValue).count();
        if (correctCount != 1) {
            errors.add("debe tener exactamente 1 respuesta correcta (tiene " + correctCount + ")");
        }
        if (!errors.isEmpty()) {
            String message = "Pregunta inválida: " + String.join("; ", errors);
            reset();
            throw new IllegalStateException(message);
        }
    }
}
