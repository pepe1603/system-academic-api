package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional("postgresTransactionManager")
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findByUserIdAndIsVerifiedFalse(UUID userId);

    Optional<EmailVerification> findByVerificationCode(String code);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpired();

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.userId = :userId")
    void deleteByUserId(UUID userId);
}