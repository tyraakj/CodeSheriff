package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.AuditTrail;
import com.backend.CodeSheriff.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditTrail entity.
 * Provides CRUD operations and custom queries for audit trail management.
 * 
 * Note: AuditTrail is an append-only entity. Deletion operations should
 * only be performed with service role credentials that bypass RLS.
 */
@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, UUID> {

    /**
     * Find all audit entries for a specific user.
     * @param user the user
     * @return list of audit trail entries
     */
    List<AuditTrail> findByUser(User user);

    /**
     * Find all audit entries for a user ID.
     * @param userId the user ID
     * @return list of audit trail entries
     */
    List<AuditTrail> findByUserUserId(UUID userId);

    /**
     * Find audit entries by action type.
     * @param actionType the action type
     * @return list of audit trail entries
     */
    List<AuditTrail> findByActionType(String actionType);

    /**
     * Find audit entries by resource type.
     * @param resourceType the resource type
     * @return list of audit trail entries
     */
    List<AuditTrail> findByResourceType(String resourceType);

    /**
     * Find audit entries by resource ID.
     * @param resourceId the resource ID
     * @return list of audit trail entries
     */
    List<AuditTrail> findByResourceId(UUID resourceId);

    /**
     * Find audit entries by status.
     * @param status the status
     * @return list of audit trail entries
     */
    List<AuditTrail> findByStatus(String status);

    /**
     * Find failed audit entries.
     * @return list of failed audit trail entries
     */
    List<AuditTrail> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find audit entries by request ID (correlation ID).
     * @param requestId the request ID
     * @return list of related audit trail entries
     */
    List<AuditTrail> findByRequestId(UUID requestId);

    /**
     * Find audit entries created within a date range.
     * @param startDate start of the range
     * @param endDate end of the range
     * @return list of audit trail entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.createdAt BETWEEN :startDate AND :endDate ORDER BY at.createdAt DESC")
    List<AuditTrail> findEntriesCreatedBetween(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Find audit entries by user and action type.
     * @param userId the user ID
     * @param actionType the action type
     * @return list of audit trail entries
     */
    List<AuditTrail> findByUserUserIdAndActionType(UUID userId, String actionType);

    /**
     * Find audit entries by IP address.
     * @param ipAddress the IP address
     * @return list of audit trail entries
     */
    List<AuditTrail> findByIpAddress(String ipAddress);

    /**
     * Find security-related audit entries.
     * @return list of security-related audit trail entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.actionType LIKE '%security%' OR at.actionType LIKE '%login%' OR at.actionType LIKE '%auth%'")
    List<AuditTrail> findSecurityRelatedEntries();

    /**
     * Find audit entries with errors.
     * @return list of audit trail entries with errors
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.errorMessage IS NOT NULL AND at.errorMessage != ''")
    List<AuditTrail> findEntriesWithErrors();

    /**
     * Find recent audit entries for a user.
     * @param userId the user ID
     * @param limit maximum number of entries
     * @return list of recent audit trail entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.user.userId = :userId ORDER BY at.createdAt DESC LIMIT :limit")
    List<AuditTrail> findRecentByUser(@Param("userId") UUID userId, @Param("limit") int limit);

    /**
     * Find recent audit entries.
     * @param limit maximum number of entries
     * @return list of recent audit trail entries
     */
    @Query("SELECT at FROM AuditTrail at ORDER BY at.createdAt DESC LIMIT :limit")
    List<AuditTrail> findRecentEntries(@Param("limit") int limit);

    /**
     * Count audit entries by action type.
     * @param actionType the action type
     * @return count of entries
     */
    long countByActionType(String actionType);

    /**
     * Count audit entries by status.
     * @param status the status
     * @return count of entries
     */
    long countByStatus(String status);

    /**
     * Count audit entries for a user.
     * @param userId the user ID
     * @return count of entries
     */
    long countByUserUserId(UUID userId);

    /**
     * Find audit entries grouped by action type with counts.
     * @return list of action type statistics
     */
    @Query("SELECT at.actionType, COUNT(at) FROM AuditTrail at GROUP BY at.actionType ORDER BY COUNT(at) DESC")
    List<Object[]> countEntriesByActionType();

    /**
     * Find audit entries grouped by status with counts.
     * @return list of status statistics
     */
    @Query("SELECT at.status, COUNT(at) FROM AuditTrail at GROUP BY at.status ORDER BY COUNT(at) DESC")
    List<Object[]> countEntriesByStatus();

    /**
     * Get average action duration by action type.
     * @param actionType the action type
     * @return average duration in milliseconds
     */
    @Query("SELECT AVG(at.durationMs) FROM AuditTrail at WHERE at.actionType = :actionType AND at.durationMs IS NOT NULL")
    Double getAverageDurationByActionType(@Param("actionType") String actionType);

    /**
     * Find slow operations (high duration).
     * @param minDuration minimum duration threshold in milliseconds
     * @return list of slow audit trail entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.durationMs >= :minDuration ORDER BY at.durationMs DESC")
    List<AuditTrail> findSlowOperations(@Param("minDuration") long minDuration);

    /**
     * Find audit entries by user agent pattern.
     * @param pattern the user agent pattern
     * @return list of audit trail entries
     */
    List<AuditTrail> findByUserAgentContainingIgnoreCase(String pattern);

    /**
     * Find all login attempts for a user.
     * @param userId the user ID
     * @return list of login audit entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.user.userId = :userId AND at.actionType LIKE '%login%' ORDER BY at.createdAt DESC")
    List<AuditTrail> findLoginAttemptsByUser(@Param("userId") UUID userId);

    /**
     * Find failed login attempts.
     * @return list of failed login audit entries
     */
    @Query("SELECT at FROM AuditTrail at WHERE at.actionType LIKE '%login%' AND at.status = 'failure' ORDER BY at.createdAt DESC")
    List<AuditTrail> findFailedLoginAttempts();

    /**
     * Find suspicious activity (multiple failures from same IP).
     * @param ipAddress the IP address
     * @param minFailures minimum number of failures
     * @return count of failures
     */
    @Query("SELECT COUNT(at) FROM AuditTrail at WHERE at.ipAddress = :ipAddress AND at.status = 'failure'")
    long countFailuresByIpAddress(@Param("ipAddress") String ipAddress);
}

// Made with Bob
