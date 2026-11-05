package co.edu.unicauca.question.adapter;

import co.edu.unicauca.question.domain.model.Question;
import java.util.List;

/** Patrón Adapter - interfaz objetivo (Target): la que entiende el banco para obtener preguntas de cualquier fuente. */
public interface QuestionProvider {
    /** Entrega las preguntas de la fuente ya convertidas al modelo del banco. */
    List<Question> getQuestions();
}
