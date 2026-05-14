package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Certificate;
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
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    Page<Certificate> findAllByIsDeletedFalse(Pageable pageable);

    Page<Certificate> findAllByIsDeletedTrue(Pageable pageable);

    List<Certificate> findByStudentIdAndIsDeletedFalse(UUID studentId);

    Optional<Certificate> findByOfficialFolioAndIsDeletedFalse(String officialFolio);

    boolean existsByOfficialFolioAndIsDeletedFalse(String officialFolio);
}
