package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OptionEmbeddable {

    @Column(name = "option_label", nullable = false, length = 4)
    private String label;

    @Column(name = "option_text", nullable = false, length = 1000)
    private String text;

    protected OptionEmbeddable() {
    }

    public OptionEmbeddable(String label, String text) {
        this.label = label;
        this.text = text;
    }

    public String getLabel() { return label; }
    public String getText() { return text; }
}
