package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.AttendancePeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface AttendancePeriodRepository extends JpaRepository<AttendancePeriod, UUID> {

    Optional<AttendancePeriod> findByEnrollmentIdAndAcademicSemesterId(UUID enrollmentId, UUID academicSemesterId);

    Page<AttendancePeriod> findByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId, Pageable pageable);

    Page<AttendancePeriod> findByAcademicSemesterIdOrderByCreatedAtDesc(UUID academicSemesterId, Pageable pageable);

    boolean existsByEnrollmentIdAndAcademicSemesterId(UUID enrollmentId, UUID academicSemesterId);
}
