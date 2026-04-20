package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, UUID> {

    Optional<RegistrationRequest> findByCurp(String curp);

    Optional<RegistrationRequest> findByEmail(String email);

    List<RegistrationRequest> findByStatus(RegistrationRequest.RegistrationStatus status);

    boolean existsByCurp(String curp);

    boolean existsByEmail(String email);
}