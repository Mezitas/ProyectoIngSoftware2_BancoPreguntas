package co.edu.unicauca.lifecycle.repository;

import co.edu.unicauca.lifecycle.model.OperationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationResultRepository extends JpaRepository<OperationResultEntity, String> {
}
