package co.edu.unicauca.question.repository;

import co.edu.unicauca.question.model.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    boolean existsByExternalCode(String externalCode);
}
