package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.ReportCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface ReportCardDetailRepository extends JpaRepository<ReportCardDetail, UUID> {

    List<ReportCardDetail> findByReportCardId(UUID reportCardId);

    List<ReportCardDetail> findByReportCardIdIn(List<UUID> reportCardIds);

    void deleteByReportCardId(UUID reportCardId);
}
