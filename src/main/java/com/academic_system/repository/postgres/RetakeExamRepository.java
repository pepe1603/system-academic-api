package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.RetakeExam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface RetakeExamRepository extends JpaRepository<RetakeExam, UUID> {

    Page<RetakeExam> findAllByIsDeletedFalse(Pageable pageable);

    Page<RetakeExam> findAllByIsDeletedTrue(Pageable pageable);

    List<RetakeExam> findByStudentIdAndIsDeletedFalse(UUID studentId);

    List<RetakeExam> findByCourseIdAndIsDeletedFalse(UUID courseId);

    List<RetakeExam> findByAcademicSemesterIdAndIsDeletedFalse(UUID academicSemesterId);

    boolean existsByStudentIdAndCourseIdAndAcademicSemesterIdAndIsDeletedFalse(
            UUID studentId, UUID courseId, UUID academicSemesterId);
}
