package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByCurp(String curp);

    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);

    Optional<Student> findByUserId(UUID userId);

    boolean existsByCurp(String curp);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    boolean existsByCurpAndIsDeletedFalse(String curp);

    boolean existsByEnrollmentNumberAndIsDeletedFalse(String enrollmentNumber);

    boolean existsByCurpAndIsDeletedFalseAndIdNot(String curp, UUID id);

    boolean existsByEnrollmentNumberAndIsDeletedFalseAndIdNot(String enrollmentNumber, UUID id);

    Optional<Student> findByCurpAndIsActiveTrueAndIsDeletedFalse(String curp);

    Optional<Student> findByCurpAndIsDeletedFalse(String curp);

    Page<Student> findAllByIsDeletedFalse(Pageable pageable);

    Page<Student> findAllByIsDeletedTrue(Pageable pageable);
}
