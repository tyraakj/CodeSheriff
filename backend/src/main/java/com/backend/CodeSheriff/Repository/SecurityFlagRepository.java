package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.SecurityFlag;
import com.backend.CodeSheriff.Entity.SecurityScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for SecurityFlag entity.
 * Provides CRUD operations and custom queries for security flag management.
 */
@Repository
public interface SecurityFlagRepository extends JpaRepository<SecurityFlag, UUID> {

    /**
     * Find all flags for a specific security scan.
     * @param securityScan the security scan
     * @return list of security flags
     */
    List<SecurityFlag> findBySecurityScan(SecurityScan securityScan);

    /**
     * Find all flags for a scan ID.
     * @param scanId the scan ID
     * @return list of security flags
     */
    List<SecurityFlag> findBySecurityScanScanId(UUID scanId);

    /**
     * Find flags by severity.
     * @param severity the severity level
     * @return list of security flags
     */
    List<SecurityFlag> findBySeverity(String severity);

    /**
     * Find flags by type.
     * @param flagType the flag type
     * @return list of security flags
     */
    List<SecurityFlag> findByFlagType(String flagType);

    /**
     * Find critical flags.
     * @return list of critical security flags
     */
    List<SecurityFlag> findBySeverityOrderByCreatedAtDesc(String severity);

    /**
     * Find unresolved flags.
     * @return list of unresolved security flags
     */
    List<SecurityFlag> findByIsResolvedFalse();

    /**
     * Find resolved flags.
     * @return list of resolved security flags
     */
    List<SecurityFlag> findByIsResolvedTrue();

    /**
     * Find false positives.
     * @return list of flags marked as false positives
     */
    List<SecurityFlag> findByIsFalsePositiveTrue();

    /**
     * Find flags by class name.
     * @param className the class name
     * @return list of security flags
     */
    List<SecurityFlag> findByClassName(String className);

    /**
     * Find flags by method name.
     * @param methodName the method name
     * @return list of security flags
     */
    List<SecurityFlag> findByMethodName(String methodName);

    /**
     * Find flags by CWE ID.
     * @param cweId the CWE ID
     * @return list of security flags
     */
    List<SecurityFlag> findByCweId(String cweId);

    /**
     * Find flags by OWASP category.
     * @param owaspCategory the OWASP category
     * @return list of security flags
     */
    List<SecurityFlag> findByOwaspCategory(String owaspCategory);

    /**
     * Find flags with low confidence.
     * @param maxConfidence maximum confidence threshold
     * @return list of security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.confidenceScore < :maxConfidence")
    List<SecurityFlag> findLowConfidenceFlags(@Param("maxConfidence") int maxConfidence);

    /**
     * Find flags with high confidence.
     * @param minConfidence minimum confidence threshold
     * @return list of security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.confidenceScore >= :minConfidence")
    List<SecurityFlag> findHighConfidenceFlags(@Param("minConfidence") int minConfidence);

    /**
     * Find unresolved critical or high severity flags.
     * @return list of high-priority security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.isResolved = false AND sf.severity IN ('critical', 'high')")
    List<SecurityFlag> findUnresolvedHighPriorityFlags();

    /**
     * Find flags by scan type and severity.
     * @param scanType the scan type
     * @param severity the severity level
     * @return list of security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.securityScan.scanType = :scanType AND sf.severity = :severity")
    List<SecurityFlag> findByScanTypeAndSeverity(
        @Param("scanType") String scanType,
        @Param("severity") String severity
    );

    /**
     * Count flags by severity.
     * @param severity the severity level
     * @return count of flags
     */
    long countBySeverity(String severity);

    /**
     * Count flags by type.
     * @param flagType the flag type
     * @return count of flags
     */
    long countByFlagType(String flagType);

    /**
     * Count unresolved flags.
     * @return count of unresolved flags
     */
    long countByIsResolvedFalse();

    /**
     * Find all flags for an analysis.
     * @param analysisId the analysis ID
     * @return list of security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.securityScan.analysis.analysisId = :analysisId")
    List<SecurityFlag> findByAnalysisId(@Param("analysisId") UUID analysisId);

    /**
     * Find flags by file path pattern.
     * @param pattern the file path pattern
     * @return list of security flags
     */
    List<SecurityFlag> findByFilePathContainingIgnoreCase(String pattern);

    /**
     * Find flags grouped by type with counts.
     * @return list of flag type statistics
     */
    @Query("SELECT sf.flagType, COUNT(sf) FROM SecurityFlag sf GROUP BY sf.flagType ORDER BY COUNT(sf) DESC")
    List<Object[]> countFlagsByType();

    /**
     * Find flags grouped by severity with counts.
     * @return list of severity statistics
     */
    @Query("SELECT sf.severity, COUNT(sf) FROM SecurityFlag sf GROUP BY sf.severity ORDER BY COUNT(sf) DESC")
    List<Object[]> countFlagsBySeverity();

    /**
     * Find all flags for a user.
     * @param userId the user ID
     * @return list of security flags
     */
    @Query("SELECT sf FROM SecurityFlag sf WHERE sf.securityScan.analysis.user.userId = :userId")
    List<SecurityFlag> findByUserId(@Param("userId") UUID userId);
}


