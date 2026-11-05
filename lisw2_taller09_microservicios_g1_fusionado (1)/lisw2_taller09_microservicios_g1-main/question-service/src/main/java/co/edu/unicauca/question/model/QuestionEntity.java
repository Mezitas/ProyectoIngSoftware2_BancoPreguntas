package co.edu.unicauca.question.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Contenido de la pregunta. Este servicio NO guarda el estado del ciclo de vida:
 * esa responsabilidad es exclusiva de Lifecycle Service.
 */
@Entity
@Table(name = "questions")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "question_context", length = 3000)
    private String context;

    @Column(name = "question_statement", length = 3000)
    private String statement;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_order")
    private List<OptionEmbeddable> options = new ArrayList<>();

    @Column(name = "correct_option", length = 4)
    private String correctOption;

    @Column(length = 3000)
    private String justification;

    private String competency;
    private String topic;
    private String subtopic;

    @Column(length = 3000)
    private String bibliography;

    @Column(length = 32)
    private String difficulty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void updateContent(String context, String statement, List<OptionEmbeddable> options, String correctOption,
                              String justification, String competency, String topic, String subtopic,
                              String bibliography, String difficulty) {
        this.context = context;
        this.statement = statement;
        this.options.clear();
        this.options.addAll(options);
        this.correctOption = correctOption;
        this.justification = justification;
        this.competency = competency;
        this.topic = topic;
        this.subtopic = subtopic;
        this.bibliography = bibliography;
        this.difficulty = difficulty;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getContext() { return context; }
    public String getStatement() { return statement; }
    public List<OptionEmbeddable> getOptions() { return options; }
    public String getCorrectOption() { return correctOption; }
    public String getJustification() { return justification; }
    public String getCompetency() { return competency; }
    public String getTopic() { return topic; }
    public String getSubtopic() { return subtopic; }
    public String getBibliography() { return bibliography; }
    public String getDifficulty() { return difficulty; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Column(name = "external_code", length = 64)
    private String externalCode;

    public String getExternalCode() { return externalCode; }
    public void setExternalCode(String externalCode) { this.externalCode = externalCode; }
}
