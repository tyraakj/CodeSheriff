package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.BobOutput;
import com.backend.CodeSheriff.Entity.Method;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for BobOutput entity.
 * Provides CRUD operations and custom queries for Bob AI analysis results.
 */
@Repository
public interface BobOutputRepository extends JpaRepository<BobOutput, UUID> {

    /**
     * Find Bob output for a specific method.
     * @param method the method
     * @return Optional containing the Bob output if found
     */
    Optional<BobOutput> findByMethod(Method method);

    /**
     * Find Bob output by method ID.
     * @param methodId the method ID
     * @return Optional containing the Bob output if found
     */
    Optional<BobOutput> findByMethodMethodId(UUID methodId);

    Page<BobOutput> findAllByMethodMethodId(UUID methodId, Pageable pageable);

    /**
     * Find all outputs by risk level.
     * @param riskLevel the risk level (low, medium, high, critical)
     * @return list of Bob outputs
     */
    List<BobOutput> findByRiskLevel(String riskLevel);

    /**
     * Find high-risk outputs (high or critical).
     * @return list of high-risk Bob outputs
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.riskLevel IN ('high', 'critical')")
    List<BobOutput> findHighRiskOutputs();

    /**
     * Find outputs with security concerns.
     * @return list of Bob outputs with security issues
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.securityConcerns IS NOT NULL AND bo.securityConcerns != ''")
    List<BobOutput> findOutputsWithSecurityConcerns();

    /**
     * Find outputs by complexity assessment.
     * @param complexityAssessment the complexity level
     * @return list of Bob outputs
     */
    List<BobOutput> findByComplexityAssessment(String complexityAssessment);

    /**
     * Find outputs with low confidence scores.
     * @param maxConfidence maximum confidence threshold
     * @return list of Bob outputs
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.confidenceScore < :maxConfidence")
    List<BobOutput> findLowConfidenceOutputs(@Param("maxConfidence") int maxConfidence);

    /**
     * Find outputs with high confidence scores.
     * @param minConfidence minimum confidence threshold
     * @return list of Bob outputs
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.confidenceScore >= :minConfidence")
    List<BobOutput> findHighConfidenceOutputs(@Param("minConfidence") int minConfidence);

    /**
     * Find outputs for methods without tests.
     * @return list of Bob outputs
     */
    List<BobOutput> findByHasTestsFalse();

    /**
     * Find outputs with low maintainability scores.
     * @param maxScore maximum maintainability score
     * @return list of Bob outputs
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.maintainabilityScore < :maxScore")
    List<BobOutput> findLowMaintainabilityOutputs(@Param("maxScore") int maxScore);

    /**
     * Find outputs by model used.
     * @param modelUsed the model name
     * @return list of Bob outputs
     */
    List<BobOutput> findByModelUsed(String modelUsed);

    /**
     * Find outputs with errors.
     * @return list of Bob outputs that had errors
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.errorMessage IS NOT NULL AND bo.errorMessage != ''")
    List<BobOutput> findOutputsWithErrors();

    /**
     * Find all outputs for an analysis.
     * @param analysisId the analysis ID
     * @return list of Bob outputs
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.method.javaClass.analysis.analysisId = :analysisId")
    List<BobOutput> findByAnalysisId(@Param("analysisId") UUID analysisId);

    /**
     * Count outputs by risk level.
     * @param riskLevel the risk level
     * @return count of outputs
     */
    long countByRiskLevel(String riskLevel);

    /**
     * Get average confidence score.
     * @return average confidence score
     */
    @Query("SELECT AVG(bo.confidenceScore) FROM BobOutput bo WHERE bo.confidenceScore IS NOT NULL")
    Double getAverageConfidenceScore();

    /**
     * Get average maintainability score.
     * @return average maintainability score
     */
    @Query("SELECT AVG(bo.maintainabilityScore) FROM BobOutput bo WHERE bo.maintainabilityScore IS NOT NULL")
    Double getAverageMaintainabilityScore();

    /**
     * Find outputs with performance notes.
     * @return list of Bob outputs with performance concerns
     */
    @Query("SELECT bo FROM BobOutput bo WHERE bo.performanceNotes IS NOT NULL AND bo.performanceNotes != ''")
    List<BobOutput> findOutputsWithPerformanceNotes();
}

// Made with Bob
