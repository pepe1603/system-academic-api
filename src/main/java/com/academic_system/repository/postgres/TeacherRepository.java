package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByRfc(String rfc);

    Optional<Teacher> findByCurp(String curp);

    Optional<Teacher> findByEmployeeNumber(String employeeNumber);

    Optional<Teacher> findByUserId(UUID userId);

    boolean existsByRfc(String rfc);

    boolean existsByCurp(String curp);

    boolean existsByEmployeeNumber(String employeeNumber);

    Optional<Teacher> findByCurpAndIsActiveTrueAndIsDeletedFalse(String curp);

    Optional<Teacher> findByCurpAndIsDeletedFalse(String curp);

    Page<Teacher> findAllByIsDeletedFalse(Pageable pageable);

    Page<Teacher> findAllByIsDeletedTrue(Pageable pageable);
}
