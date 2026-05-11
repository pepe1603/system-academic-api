package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.ConductIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface ConductIncidentRepository extends JpaRepository<ConductIncident, UUID> {

    List<ConductIncident> findByEnrollmentIdAndIsDeletedFalse(UUID enrollmentId);

    List<ConductIncident> findByEnrollmentId(UUID enrollmentId);
}
