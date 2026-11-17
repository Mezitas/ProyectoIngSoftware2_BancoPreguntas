package co.edu.unicauca.user.repository;

import co.edu.unicauca.user.model.UserEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    /** Usuarios que tienen el rol indicado (p. ej. ADMIN para notificar nuevas preguntas pendientes). */
    @Query("select distinct u from UserEntity u join u.roles r where r = :role order by u.id")
    List<UserEntity> findByRole(@Param("role") String role);
}
