package co.edu.unicauca.question.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pregunta (reactivo) del Banco de Preguntas Saber PRO. Objeto complejo e inmutable:
 * sus partes se ensamblan con el patrón Builder ({@code QuestionBuilder}).
 */
public class Question {
    private final String id;
    private final Context context;
    private final String statement;
    private final List<Option> options;
    private final String justification;
    private final String competency;
    private final String topic;
    private final String subtopic;

    public Question(String id, Context context, String statement, List<Option> options,
                    String justification, String competency, String topic, String subtopic) {
        this.id = id;
        this.context = context;
        this.statement = statement;
        this.options = Collections.unmodifiableList(options);
        this.justification = justification;
        this.competency = competency;
        this.topic = topic;
        this.subtopic = subtopic;
    }

    public String getId() { return id; }
    public Context getContext() { return context; }
    public String getStatement() { return statement; }
    public List<Option> getOptions() { return options; }
    public String getJustification() { return justification; }
    public String getCompetency() { return competency; }
    public String getTopic() { return topic; }
    public String getSubtopic() { return subtopic; }

    public Optional<Option> getCorrectOption() {
        return options.stream().filter(Option::isCorrect).findFirst();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(id).append("] ").append(topic).append(" / ").append(subtopic)
          .append("  (").append(competency).append(")\n");
        sb.append("  Contexto: ").append(context).append("\n");
        sb.append("  Enunciado: ").append(statement).append("\n");
        options.forEach(o -> sb.append("    ").append(o).append("\n"));
        sb.append("  Justificación: ").append(justification);
        return sb.toString();
    }
}
