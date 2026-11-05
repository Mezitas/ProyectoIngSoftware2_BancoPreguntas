package co.edu.unicauca.question.builder;

import co.edu.unicauca.question.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/** Patrón Builder - Director: conoce la "receta" (orden de pasos) y se la pide a cualquier {@link IQuestionBuilder}. */
public class QuestionDirector {
    private final IQuestionBuilder builder;

    public QuestionDirector(IQuestionBuilder builder) { this.builder = builder; }

    /** Arma una pregunta con la respuesta correcta en la última posición. */
    public Question buildMultipleChoice(String context, String statement, String correctAnswer,
                                        List<String> distractors, String justification,
                                        String competency, String topic, String subtopic) {
        return buildMultipleChoice(context, statement, correctAnswer, distractors, Integer.MAX_VALUE,
                justification, competency, topic, subtopic);
    }

    /** Arma una pregunta ubicando la respuesta correcta en la posición indicada (0 = A, 1 = B, ...). */
    public Question buildMultipleChoice(String context, String statement, String correctAnswer,
                                        List<String> distractors, int correctPosition, String justification,
                                        String competency, String topic, String subtopic) {
        List<String> texts = new ArrayList<>(distractors);
        int position = Math.max(0, Math.min(correctPosition, texts.size()));
        texts.add(position, correctAnswer);

        builder.setContext(context).setStatement(statement);
        for (int i = 0; i < texts.size(); i++) {
            builder.addOption(texts.get(i), i == position);
        }
        return builder.setJustification(justification)
                .setClassification(competency, topic, subtopic)
                .build();
    }

    /** Pregunta de ejemplo para demostraciones y pruebas. */
    public Question buildSampleQuestion() {
        return buildMultipleChoice(
                "Un equipo de software necesita crear objetos complejos paso a paso, evitando constructores con muchos parámetros.",
                "¿Qué patrón de diseño resuelve mejor esta necesidad?",
                "Builder",
                List.of("Singleton", "Observer", "Adapter"),
                0,
                "Builder separa la construcción de un objeto complejo de su representación, permitiendo ensamblarlo por pasos.",
                "Diseño de software", "Patrones de diseño", "Patrones creacionales");
    }
}
