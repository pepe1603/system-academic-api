package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0 WHERE u.id = :userId")
    void resetFailedAttempts(UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = u.failedAttempts + 1, u.isLocked = CASE WHEN u.failedAttempts + 1 >= 5 THEN true ELSE u.isLocked END WHERE u.id = :userId")
    void incrementFailedAttempts(UUID userId);

    // Excluir soft-deleted en consultas por defecto
    @Query("SELECT u FROM User u WHERE u.username = :username AND (u.isDeleted IS NULL OR u.isDeleted = false)")
    Optional<User> findByUsernameAndNotDeleted(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND (u.isDeleted IS NULL OR u.isDeleted = false)")
    Optional<User> findByEmailAndNotDeleted(String email);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByEmailAndIsDeletedFalse(String email);

    // Para listar usuarios (excluir borrados)
    Page<User> findAllByIsDeletedFalseOrIsDeletedIsNull(Pageable pageable);

    // Para listar usuarios borrados
    Page<User> findAllByIsDeletedTrue(Pageable pageable);
}
