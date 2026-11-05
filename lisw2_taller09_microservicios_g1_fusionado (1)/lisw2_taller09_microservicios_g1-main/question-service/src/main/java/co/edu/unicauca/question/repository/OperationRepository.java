package co.edu.unicauca.question.repository;

import co.edu.unicauca.question.model.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<OperationEntity, String> {
}
