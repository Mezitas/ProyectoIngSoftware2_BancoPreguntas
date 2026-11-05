package co.edu.unicauca.lifecycle.repository;

import co.edu.unicauca.lifecycle.model.QuestionStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStateRepository extends JpaRepository<QuestionStateEntity, Long> {
}
