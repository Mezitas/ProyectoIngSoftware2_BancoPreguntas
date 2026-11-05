package co.edu.unicauca.question.domain.model;

/** Contexto (situación, texto o caso) que enmarca una pregunta Saber PRO. */
public class Context {
    private final String text;

    public Context(String text) {
        this.text = text == null ? "" : text.trim();
    }

    public String getText() {
        return text;
    }

    public boolean isBlank() {
        return text.isEmpty();
    }

    @Override
    public String toString() {
        return text;
    }
}
