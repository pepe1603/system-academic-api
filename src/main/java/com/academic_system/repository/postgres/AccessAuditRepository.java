package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.AccessAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface AccessAuditRepository extends JpaRepository<AccessAudit, UUID> {

    Page<AccessAudit> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AccessAudit> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AccessAudit> findByModuleOrderByCreatedAtDesc(String module, Pageable pageable);

    Page<AccessAudit> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AccessAudit> findBySuccessOrderByCreatedAtDesc(Boolean success, Pageable pageable);
}
