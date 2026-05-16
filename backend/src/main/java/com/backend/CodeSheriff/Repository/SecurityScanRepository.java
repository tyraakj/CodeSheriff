package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.Analysis;
import com.backend.CodeSheriff.Entity.SecurityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SecurityScan entity.
 * Provides CRUD operations and custom queries for security scan management.
 */
@Repository
public interface SecurityScanRepository extends JpaRepository<SecurityScan, UUID> {

    /**
     * Find all scans for a specific analysis.
     * @param analysis the analysis
     * @return list of security scans
     */
    List<SecurityScan> findByAnalysis(Analysis analysis);

    /**
     * Find all scans for an analysis ID.
     * @param analysisId the analysis ID
     * @return list of security scans
     */
    List<SecurityScan> findByAnalysisAnalysisId(UUID analysisId);

    Page<SecurityScan> findByAnalysisAnalysisId(UUID analysisId, Pageable pageable);

    /**
     * Find scans by type.
     * @param scanType the scan type
     * @return list of security scans
     */
    List<SecurityScan> findByScanType(String scanType);

    /**
     * Find scans by status.
     * @param status the status
     * @return list of security scans
     */
    List<SecurityScan> findByStatus(String status);

    /**
     * Find scans by analysis and type.
     * @param analysisId the analysis ID
     * @param scanType the scan type
     * @return Optional containing the scan if found
     */
    Optional<SecurityScan> findByAnalysisAnalysisIdAndScanType(UUID analysisId, String scanType);

    /**
     * Find scans with critical flags.
     * @return list of security scans with critical issues
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.criticalFlags > 0")
    List<SecurityScan> findScansWithCriticalFlags();

    /**
     * Find scans with high severity flags.
     * @return list of security scans with high severity issues
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.criticalFlags > 0 OR ss.highFlags > 0")
    List<SecurityScan> findScansWithHighSeverityFlags();

    /**
     * Find completed scans.
     * @return list of completed security scans
     */
    List<SecurityScan> findByStatusOrderByCompletedAtDesc(String status);

    /**
     * Find scans created within a date range.
     * @param startDate start of the range
     * @param endDate end of the range
     * @return list of security scans
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.createdAt BETWEEN :startDate AND :endDate")
    List<SecurityScan> findScansCreatedBetween(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Find the most recent scan for an analysis.
     * @param analysisId the analysis ID
     * @return Optional containing the most recent scan
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.analysis.analysisId = :analysisId ORDER BY ss.createdAt DESC")
    Optional<SecurityScan> findMostRecentByAnalysis(@Param("analysisId") UUID analysisId);

    /**
     * Find scans with errors.
     * @return list of security scans that had errors
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.errorMessage IS NOT NULL AND ss.errorMessage != ''")
    List<SecurityScan> findScansWithErrors();

    /**
     * Count scans by status.
     * @param status the status
     * @return count of scans
     */
    long countByStatus(String status);

    /**
     * Count scans by type.
     * @param scanType the scan type
     * @return count of scans
     */
    long countByScanType(String scanType);

    /**
     * Find scans with no flags.
     * @return list of security scans with zero flags
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.totalFlags = 0")
    List<SecurityScan> findScansWithNoFlags();

    /**
     * Find scans with flags above threshold.
     * @param minFlags minimum number of flags
     * @return list of security scans
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.totalFlags >= :minFlags ORDER BY ss.totalFlags DESC")
    List<SecurityScan> findScansWithHighFlagCount(@Param("minFlags") int minFlags);

    /**
     * Get average scan duration by type.
     * @param scanType the scan type
     * @return average duration in milliseconds
     */
    @Query("SELECT AVG(ss.durationMs) FROM SecurityScan ss WHERE ss.scanType = :scanType AND ss.durationMs IS NOT NULL")
    Double getAverageDurationByScanType(@Param("scanType") String scanType);

    /**
     * Find incomplete scans (not completed or failed).
     * @return list of incomplete security scans
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.status NOT IN ('completed', 'failed')")
    List<SecurityScan> findIncompleteScans();

    /**
     * Find all scans for a user.
     * @param userId the user ID
     * @return list of security scans
     */
    @Query("SELECT ss FROM SecurityScan ss WHERE ss.analysis.user.userId = :userId")
    List<SecurityScan> findByUserId(@Param("userId") UUID userId);
}

// Made with Bob
