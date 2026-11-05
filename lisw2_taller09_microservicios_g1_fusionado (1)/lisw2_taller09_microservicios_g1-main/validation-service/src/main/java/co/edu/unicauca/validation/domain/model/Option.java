package co.edu.unicauca.validation.domain.model;

/** Opción de respuesta de una pregunta (A, B, C o D). */
public class Option {
    private final String label;
    private final String text;
    private final boolean correct;

    public Option(String label, String text, boolean correct) {
        this.label = label;
        this.text = text == null ? "" : text.trim();
        this.correct = correct;
    }

    public String getLabel() {
        return label;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }

    @Override
    public String toString() {
        return label + ") " + text + (correct ? "  [correcta]" : "");
    }
}
