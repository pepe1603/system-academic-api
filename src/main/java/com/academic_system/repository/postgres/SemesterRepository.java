package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Semester;
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
public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    Page<Semester> findAllByIsDeletedFalse(Pageable pageable);

    Page<Semester> findAllByIsDeletedTrue(Pageable pageable);

    List<Semester> findByStudyPlanIdAndIsDeletedFalse(UUID studyPlanId);

    Optional<Semester> findByStudyPlanIdAndSemesterNumberAndIsDeletedFalse(UUID studyPlanId, Integer semesterNumber);
}
