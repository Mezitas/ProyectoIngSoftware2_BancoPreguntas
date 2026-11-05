package co.edu.unicauca.question.adapter;

import co.edu.unicauca.question.builder.IQuestionBuilder;
import co.edu.unicauca.question.builder.QuestionBuilder;
import co.edu.unicauca.question.domain.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Patrón Adapter (de objeto): adapta {@link ExternalQuestionService} (Adaptee) a {@link QuestionProvider} (Target).
 */
public class ExternalQuestionAdapter implements QuestionProvider {
    private static final String LABELS = "ABCD";
    private final ExternalQuestionService service;
    private final List<String> warnings = new ArrayList<>();

    public ExternalQuestionAdapter(ExternalQuestionService service) { this.service = service; }

    @Override
    public List<Question> getQuestions() {
        warnings.clear();
        List<Question> questions = new ArrayList<>();
        questions.addAll(fromJson(service.fetchQuestionsJson()));
        for (int i = 0; i < service.rawQuestionCount(); i++) {
            fromRaw(service.fetchRawQuestion(i)).ifPresent(questions::add);
        }
        return questions;
    }

    public List<String> getWarnings() { return Collections.unmodifiableList(warnings); }

    private List<Question> fromJson(String json) {
        List<Question> result = new ArrayList<>();
        Object parsed;
        try {
            parsed = MiniJsonParser.parse(json);
        } catch (IllegalArgumentException e) {
            warnings.add("No se pudo leer el JSON externo: " + e.getMessage());
            return result;
        }
        if (!(parsed instanceof List<?> items)) {
            warnings.add("El JSON externo no es un arreglo de reactivos");
            return result;
        }
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> map)) {
                warnings.add("Reactivo externo omitido: no es un objeto JSON");
                continue;
            }
            try {
                result.add(fromJsonObject(cast(map)));
            } catch (RuntimeException e) {
                warnings.add("Reactivo externo omitido: " + e.getMessage());
            }
        }
        return result;
    }

    private Question fromJsonObject(Map<String, Object> m) {
        String code = str(m, "codigo");
        List<?> answers = m.get("respuestas") instanceof List<?> l ? l : List.of();
        int correctIndex = m.get("indice_correcta") instanceof Number n ? n.intValue() : -1;

        IQuestionBuilder builder = new QuestionBuilder().setId(code)
                .setContext(str(m, "texto_contexto"))
                .setStatement(str(m, "enunciado"));

        for (int i = 0; i < answers.size(); i++) {
            builder.addOption(String.valueOf(answers.get(i)), i == correctIndex);
        }
        return builder.setJustification(str(m, "retroalimentacion"))
                .setClassification(str(m, "competencia"), str(m, "tema"), str(m, "subtema"))
                .build();
    }

    private Optional<Question> fromRaw(String raw) {
        String[] parts = raw.split("#", -1);
        if (parts.length != 9) {
            warnings.add("Reactivo crudo omitido: se esperaban 9 campos y llegaron " + parts.length);
            return Optional.empty();
        }
        try {
            String[] texts = parts[3].split(";");
            String correctLabel = parts[4].trim().toUpperCase();
            IQuestionBuilder builder = new QuestionBuilder().setId(parts[0].trim())
                    .setContext(parts[1])
                    .setStatement(parts[2]);

            for (int i = 0; i < texts.length; i++) {
                builder.addOption(texts[i], String.valueOf(LABELS.charAt(i)).equals(correctLabel));
            }
            return Optional.of(builder.setJustification(parts[5])
                    .setClassification(parts[6], parts[7], parts[8])
                    .build());
        } catch (RuntimeException e) {
            warnings.add("Reactivo crudo " + parts[0].trim() + " omitido: " + e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?, ?> map) { return (Map<String, Object>) map; }
    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? "" : v.toString();
    }
}
