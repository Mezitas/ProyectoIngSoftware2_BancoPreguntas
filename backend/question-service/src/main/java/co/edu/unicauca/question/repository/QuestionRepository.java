package co.edu.unicauca.question.repository;

import co.edu.unicauca.question.model.QuestionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionRepository
        extends JpaRepository<QuestionEntity, Long>, JpaSpecificationExecutor<QuestionEntity> {

    boolean existsByExternalCode(String externalCode);

    List<QuestionEntity> findByAuthorIdOrderByIdDesc(Long authorId);
}
