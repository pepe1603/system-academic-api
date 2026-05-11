package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Conduct;
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
public interface ConductRepository extends JpaRepository<Conduct, UUID> {

    Page<Conduct> findAllByIsDeletedFalse(Pageable pageable);

    Page<Conduct> findAllByIsDeletedTrue(Pageable pageable);

    List<Conduct> findByEnrollmentIdAndIsDeletedFalse(UUID enrollmentId);

    List<Conduct> findByAcademicSemesterIdAndIsDeletedFalse(UUID academicSemesterId);

    Optional<Conduct> findByEnrollmentIdAndAcademicSemesterIdAndIsDeletedFalse(UUID enrollmentId, UUID academicSemesterId);

    boolean existsByEnrollmentIdAndAcademicSemesterIdAndIsDeletedFalse(UUID enrollmentId, UUID academicSemesterId);
}
