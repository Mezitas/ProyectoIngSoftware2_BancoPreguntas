package co.edu.unicauca.lifecycle.repository;

import co.edu.unicauca.lifecycle.model.StateHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateHistoryRepository extends JpaRepository<StateHistoryEntity, Long> {
    List<StateHistoryEntity> findByQuestionIdOrderByIdAsc(Long questionId);
}
