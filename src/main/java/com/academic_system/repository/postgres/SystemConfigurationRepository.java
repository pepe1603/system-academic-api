package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.SystemConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, UUID> {

    Page<SystemConfiguration> findAllByIsDeletedFalse(Pageable pageable);

    Page<SystemConfiguration> findAllByIsDeletedTrue(Pageable pageable);

    Optional<SystemConfiguration> findByConfigKeyAndIsDeletedFalse(String configKey);

    boolean existsByConfigKeyAndIsDeletedFalse(String configKey);
}
