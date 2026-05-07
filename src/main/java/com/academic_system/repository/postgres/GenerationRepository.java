package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.Generation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional("postgresTransactionManager")
public interface GenerationRepository extends JpaRepository<Generation, java.util.UUID> {

    boolean existsByNameAndIsDeletedFalse(String name);

    boolean existsByNameAndIsDeletedFalseAndIdNot(String name, java.util.UUID id);

    Page<Generation> findAllByIsDeletedFalse(Pageable pageable);

    Page<Generation> findAllByIsDeletedTrue(Pageable pageable);
}
