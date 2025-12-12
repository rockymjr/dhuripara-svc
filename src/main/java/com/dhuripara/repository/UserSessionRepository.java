package com.dhuripara.repository;

import com.dhuripara.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByToken(String token);

    List<UserSession> findByUserIdAndUserTypeAndIsActiveTrue(UUID userId, String userType);

    List<UserSession> findByIsActiveTrueOrderByLastActivityDesc();

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.token = :token")
    void deactivateSessionByToken(@Param("token") String token);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId AND s.userType = :userType")
    void deactivateAllSessionsForUser(@Param("userId") UUID userId, @Param("userType") String userType);

    @Modifying
    @Query("UPDATE UserSession s SET s.lastActivity = :lastActivity WHERE s.token = :token")
    void updateLastActivity(@Param("token") String token, @Param("lastActivity") LocalDateTime lastActivity);

    @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.isActive = true AND s.userType = :userType")
    Long countActiveUsersByType(@Param("userType") String userType);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.isActive = true")
    Long countActiveSessionsForUser(@Param("userId") UUID userId);
}

