package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Kardex;
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
public interface KardexRepository extends JpaRepository<Kardex, UUID> {

    Optional<Kardex> findByStudentIdAndCourseIdAndAcademicSemesterIdAndAttemptNumberAndIsDeletedFalse(
            UUID studentId, UUID courseId, UUID academicSemesterId, Integer attemptNumber);

    boolean existsByStudentIdAndCourseIdAndAcademicSemesterIdAndAttemptNumberAndIsDeletedFalse(
            UUID studentId, UUID courseId, UUID academicSemesterId, Integer attemptNumber);

    List<Kardex> findByStudentIdAndIsDeletedFalse(UUID studentId);

    List<Kardex> findByCourseIdAndIsDeletedFalse(UUID courseId);

    Page<Kardex> findAllByIsDeletedFalse(Pageable pageable);

    Page<Kardex> findAllByIsDeletedTrue(Pageable pageable);
}
