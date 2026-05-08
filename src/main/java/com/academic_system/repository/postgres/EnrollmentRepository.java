package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndCourseIdAndAcademicPeriodIdAndIsDeletedFalse(
            UUID studentId, UUID courseId, UUID academicPeriodId);

    boolean existsByStudentIdAndCourseIdAndAcademicPeriodIdAndIsDeletedFalseAndIdNot(
            UUID studentId, UUID courseId, UUID academicPeriodId, UUID id);

    Page<Enrollment> findAllByIsDeletedFalse(Pageable pageable);

    Page<Enrollment> findAllByIsDeletedTrue(Pageable pageable);
}
