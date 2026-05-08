package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    boolean existsByEnrollmentIdAndEvaluationTypeIdAndIsDeletedFalse(UUID enrollmentId, UUID evaluationTypeId);

    boolean existsByEnrollmentIdAndEvaluationTypeIdAndIsDeletedFalseAndIdNot(UUID enrollmentId, UUID evaluationTypeId, UUID id);

    Optional<Grade> findByEnrollmentIdAndEvaluationTypeIdAndIsDeletedFalse(UUID enrollmentId, UUID evaluationTypeId);

    List<Grade> findByEnrollmentIdAndIsDeletedFalse(UUID enrollmentId);

    Page<Grade> findAllByIsDeletedFalse(Pageable pageable);

    Page<Grade> findAllByIsDeletedTrue(Pageable pageable);
}
