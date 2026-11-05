package co.edu.unicauca.question.builder;

import co.edu.unicauca.question.domain.model.Question;

/** Patrón Builder - interfaz del constructor (Builder abstracto). Pasos fluidos. */
public interface IQuestionBuilder {
    IQuestionBuilder setId(String id);
    IQuestionBuilder setContext(String context);
    IQuestionBuilder setStatement(String statement);
    /** Agrega la siguiente opción (A, B, C, D según el orden de llamada). */
    IQuestionBuilder addOption(String text, boolean correct);
    IQuestionBuilder setJustification(String justification);
    IQuestionBuilder setClassification(String competency, String topic, String subtopic);

    /**
     * Entrega la pregunta construida.
     * @throws IllegalStateException si no hay 4 opciones, 1 correcta o falta el enunciado.
     */
    Question build();
}
