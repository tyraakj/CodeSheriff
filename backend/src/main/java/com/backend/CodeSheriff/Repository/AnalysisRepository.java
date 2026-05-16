package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.Analysis;
import com.backend.CodeSheriff.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Analysis entity.
 * Provides CRUD operations and custom queries for analysis management.
 */
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    /**
     * Find all analyses for a specific user.
     * @param user the user
     * @return list of analyses
     */
    List<Analysis> findByUser(User user);

    /**
     * Find all analyses for a user ID.
     * @param userId the user ID
     * @return list of analyses
     */
    List<Analysis> findByUserUserId(UUID userId);

    /**
     * Find analyses by status.
     * @param status the status
     * @return list of analyses
     */
    List<Analysis> findByStatus(String status);

    /**
     * Find analyses by user and status.
     * @param userId the user ID
     * @param status the status
     * @return list of analyses
     */
    List<Analysis> findByUserUserIdAndStatus(UUID userId, String status);

    /**
     * Find analyses by project name (case-insensitive).
     * @param projectName the project name
     * @return list of analyses
     */
    List<Analysis> findByProjectNameContainingIgnoreCase(String projectName);

    /**
     * Find analyses created within a date range.
     * @param startDate start of the range
     * @param endDate end of the range
     * @return list of analyses
     */
    @Query("SELECT a FROM Analysis a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Analysis> findAnalysesCreatedBetween(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Find the most recent analysis for a user.
     * @param userId the user ID
     * @return Optional containing the most recent analysis
     */
    @Query("SELECT a FROM Analysis a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    Optional<Analysis> findMostRecentByUser(@Param("userId") UUID userId);

    /**
     * Count analyses by status.
     * @param status the status
     * @return count of analyses
     */
    long countByStatus(String status);

    /**
     * Count analyses for a user.
     * @param userId the user ID
     * @return count of analyses
     */
    long countByUserUserId(UUID userId);

    /**
     * Find analyses with high method counts.
     * @param minMethods minimum number of methods
     * @return list of analyses
     */
    @Query("SELECT a FROM Analysis a WHERE a.totalMethods >= :minMethods ORDER BY a.totalMethods DESC")
    List<Analysis> findAnalysesWithHighMethodCount(@Param("minMethods") int minMethods);

    /**
     * Find incomplete analyses (not completed or failed).
     * @return list of incomplete analyses
     */
    @Query("SELECT a FROM Analysis a WHERE a.status NOT IN ('completed', 'failed')")
    List<Analysis> findIncompleteAnalyses();
}

// Made with Bob
