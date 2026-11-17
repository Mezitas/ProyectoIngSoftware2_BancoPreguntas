package co.edu.unicauca.question.repository;

import co.edu.unicauca.question.dto.QuestionFilter;
import co.edu.unicauca.question.model.QuestionEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Construye la consulta dinámica del listado a partir de los filtros recibidos (los vacíos se ignoran). */
public final class QuestionSpecifications {

    private QuestionSpecifications() {
    }

    public static Specification<QuestionEntity> from(QuestionFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.authorId() != null) {
                predicates.add(cb.equal(root.get("authorId"), filter.authorId()));
            }
            if (hasText(filter.text())) {
                String like = "%" + filter.text().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("statement")), like),
                        cb.like(cb.lower(root.<String>get("context")), like),
                        cb.like(cb.lower(root.<String>get("competency")), like),
                        cb.like(cb.lower(root.<String>get("topic")), like),
                        cb.like(cb.lower(root.<String>get("subtopic")), like)));
            }
            addEquals(predicates, filter.competency(), value ->
                    cb.equal(cb.lower(root.<String>get("competency")), value));
            addEquals(predicates, filter.topic(), value ->
                    cb.equal(cb.lower(root.<String>get("topic")), value));
            addEquals(predicates, filter.subtopic(), value ->
                    cb.equal(cb.lower(root.<String>get("subtopic")), value));
            if (hasText(filter.difficulty())) {
                predicates.add(cb.equal(root.get("difficulty"), filter.difficulty().trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addEquals(List<Predicate> predicates, String raw,
                                  java.util.function.Function<String, Predicate> builder) {
        if (hasText(raw)) {
            predicates.add(builder.apply(raw.trim().toLowerCase(Locale.ROOT)));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
