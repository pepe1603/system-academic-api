package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Guardian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    Page<Guardian> findAllByIsDeletedFalse(Pageable pageable);

    Page<Guardian> findAllByIsDeletedTrue(Pageable pageable);

    List<Guardian> findByStudentIdAndIsDeletedFalse(UUID studentId);

    List<Guardian> findByStudentIdAndIsActiveTrueAndIsDeletedFalse(UUID studentId);
}
