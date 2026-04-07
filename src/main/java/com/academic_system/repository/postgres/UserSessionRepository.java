package com.academic_system.repository.postgres;

import com.academic_system.entity.postgres.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<UserSession> findByJwtTokenAndIsActiveTrue(String jwtToken);

    Optional<UserSession> findByRefreshTokenAndIsActiveTrue(String refreshToken);

    Optional<UserSession> findByJwtToken(String jwtToken);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId")
    void invalidateAllSessionsForUser(UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now")
    void invalidateExpiredSessions(LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.jwtToken = :token")
    void invalidateByToken(String token);

    long countByUserIdAndIsActiveTrue(UUID userId);
}
