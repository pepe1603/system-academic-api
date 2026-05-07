package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
}
