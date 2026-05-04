package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Optional<UserProfile> findByCurp(String curp);

    Optional<UserProfile> findByEmployeeNumber(String employeeNumber);

    Optional<UserProfile> findByEnrollmentNumber(String enrollmentNumber);
}