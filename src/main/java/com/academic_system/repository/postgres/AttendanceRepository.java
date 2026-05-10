package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    boolean existsByEnrollmentIdAndAttendanceDateAndIsDeletedFalse(UUID enrollmentId, LocalDate attendanceDate);

    Optional<Attendance> findByEnrollmentIdAndAttendanceDateAndIsDeletedFalse(UUID enrollmentId, LocalDate attendanceDate);

    List<Attendance> findByEnrollmentIdAndIsDeletedFalse(UUID enrollmentId);

    Page<Attendance> findAllByIsDeletedFalse(Pageable pageable);

    Page<Attendance> findAllByIsDeletedTrue(Pageable pageable);
}
