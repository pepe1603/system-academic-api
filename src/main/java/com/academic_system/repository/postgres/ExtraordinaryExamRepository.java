package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.ExtraordinaryExam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface ExtraordinaryExamRepository extends JpaRepository<ExtraordinaryExam, UUID> {

    Page<ExtraordinaryExam> findAllByIsDeletedFalse(Pageable pageable);

    Page<ExtraordinaryExam> findAllByIsDeletedTrue(Pageable pageable);

    List<ExtraordinaryExam> findByStudentIdAndIsDeletedFalse(UUID studentId);

    List<ExtraordinaryExam> findByCourseIdAndIsDeletedFalse(UUID courseId);

    boolean existsByStudentIdAndCourseIdAndAttemptNumberAndIsDeletedFalse(
            UUID studentId, UUID courseId, int attemptNumber);
}
