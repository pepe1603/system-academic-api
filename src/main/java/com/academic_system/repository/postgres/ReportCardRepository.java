package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.ReportCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface ReportCardRepository extends JpaRepository<ReportCard, UUID> {

    Page<ReportCard> findAllByIsDeletedFalse(Pageable pageable);

    Page<ReportCard> findAllByIsDeletedTrue(Pageable pageable);

    List<ReportCard> findByStudentIdAndIsDeletedFalse(UUID studentId);

    boolean existsByFolioAndIsDeletedFalse(String folio);
}
