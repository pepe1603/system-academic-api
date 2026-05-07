package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.AcademicGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface AcademicGroupRepository extends JpaRepository<AcademicGroup, UUID> {

    boolean existsByNameAndAcademicSemesterIdAndCourseIdAndIsDeletedFalse(
            String name, UUID academicSemesterId, UUID courseId);

    boolean existsByNameAndAcademicSemesterIdAndCourseIdAndIsDeletedFalseAndIdNot(
            String name, UUID academicSemesterId, UUID courseId, UUID id);

    Page<AcademicGroup> findAllByIsDeletedFalse(Pageable pageable);

    Page<AcademicGroup> findAllByIsDeletedTrue(Pageable pageable);
}
