package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Entity.*;
import com.backend.CodeSheriff.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for orchestrating code analysis workflows.
 * Manages the complete lifecycle of an analysis from creation to completion.
 */
@Service
@Transactional
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final JavaClassRepository javaClassRepository;
    private final MethodRepository methodRepository;
    private final BobOutputRepository bobOutputRepository;
    private final UserRepository userRepository;
    private final SecurityScanRepository securityScanRepository;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            JavaClassRepository javaClassRepository,
            MethodRepository methodRepository,
            BobOutputRepository bobOutputRepository,
            UserRepository userRepository,
            SecurityScanRepository securityScanRepository) {
        this.analysisRepository = analysisRepository;
        this.javaClassRepository = javaClassRepository;
        this.methodRepository = methodRepository;
        this.bobOutputRepository = bobOutputRepository;
        this.userRepository = userRepository;
        this.securityScanRepository = securityScanRepository;
    }

    /**
     * Create a new analysis.
     * @param userId the user ID
     * @param projectName the project name
     * @param zipFilename the ZIP filename
     * @return the created analysis
     */
    public Analysis createAnalysis(UUID userId, String projectName, String zipFilename) {
        logger.info("Creating new analysis for user {} - project: {}", userId, projectName);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Analysis analysis = new Analysis(user, projectName);
        analysis.setZipFilename(zipFilename);
        analysis.setStatus("pending");
        
        Analysis saved = analysisRepository.save(analysis);
        logger.info("Created analysis: {} for user: {}", saved.getAnalysisId(), userId);
        
        return saved;
    }

    /**
     * Start an analysis.
     * @param analysisId the analysis ID
     * @return the updated analysis
     */
    public Analysis startAnalysis(UUID analysisId) {
        logger.info("Starting analysis: {}", analysisId);
        
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        analysis.markAsStarted();
        Analysis updated = analysisRepository.save(analysis);
        
        logger.info("Analysis started: {}", analysisId);
        return updated;
    }

    /**
     * Complete an analysis successfully.
     * @param analysisId the analysis ID
     * @return the updated analysis
     */
    public Analysis completeAnalysis(UUID analysisId) {
        logger.info("Completing analysis: {}", analysisId);
        
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        analysis.markAsCompleted();
        Analysis updated = analysisRepository.save(analysis);
        
        logger.info("Analysis completed: {} - {} classes, {} methods analyzed", 
                   analysisId, analysis.getTotalClasses(), analysis.getAnalyzedMethods());
        return updated;
    }

    /**
     * Mark an analysis as failed.
     * @param analysisId the analysis ID
     * @param errorMessage the error message
     * @return the updated analysis
     */
    public Analysis failAnalysis(UUID analysisId, String errorMessage) {
        logger.error("Failing analysis: {} - {}", analysisId, errorMessage);
        
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        analysis.markAsFailed(errorMessage);
        Analysis updated = analysisRepository.save(analysis);
        
        logger.info("Analysis failed: {}", analysisId);
        return updated;
    }

    /**
     * Add a Java class to an analysis.
     * @param analysisId the analysis ID
     * @param javaClass the Java class to add
     * @return the saved Java class
     */
    public JavaClass addJavaClass(UUID analysisId, JavaClass javaClass) {
        logger.debug("Adding Java class to analysis: {} - {}", analysisId, javaClass.getClassName());
        
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        javaClass.setAnalysis(analysis);
        JavaClass saved = javaClassRepository.save(javaClass);
        
        // Update analysis counters
        analysis.setTotalClasses((analysis.getTotalClasses() == null ? 0 : analysis.getTotalClasses()) + 1);
        analysisRepository.save(analysis);
        
        logger.debug("Added Java class: {} to analysis: {}", saved.getClassId(), analysisId);
        return saved;
    }

    /**
     * Add a method to a Java class.
     * @param classId the class ID
     * @param method the method to add
     * @return the saved method
     */
    public Method addMethod(UUID classId, Method method) {
        logger.debug("Adding method to class: {} - {}", classId, method.getMethodName());
        
        JavaClass javaClass = javaClassRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Java class not found: " + classId));
        
        method.setJavaClass(javaClass);
        Method saved = methodRepository.save(method);
        
        // Update class and analysis counters
        javaClass.incrementMethodCount();
        javaClassRepository.save(javaClass);
        
        Analysis analysis = javaClass.getAnalysis();
        analysis.setTotalMethods((analysis.getTotalMethods() == null ? 0 : analysis.getTotalMethods()) + 1);
        analysisRepository.save(analysis);
        
        logger.debug("Added method: {} to class: {}", saved.getMethodId(), classId);
        return saved;
    }

    /**
     * Add Bob analysis output to a method.
     * @param methodId the method ID
     * @param bobOutput the Bob output
     * @return the saved Bob output
     */
    public BobOutput addBobOutput(UUID methodId, BobOutput bobOutput) {
        logger.debug("Adding Bob output to method: {}", methodId);
        
        Method method = methodRepository.findById(methodId)
            .orElseThrow(() -> new IllegalArgumentException("Method not found: " + methodId));
        
        bobOutput.setMethod(method);
        BobOutput saved = bobOutputRepository.save(bobOutput);
        
        // Update analysis counter
        Analysis analysis = method.getJavaClass().getAnalysis();
        analysis.incrementAnalyzedMethods();
        analysisRepository.save(analysis);
        
        logger.debug("Added Bob output: {} to method: {}", saved.getBobOutputId(), methodId);
        return saved;
    }

    /**
     * Get analysis by ID.
     * @param analysisId the analysis ID
     * @return Optional containing the analysis if found
     */
    @Transactional(readOnly = true)
    public Optional<Analysis> getAnalysis(UUID analysisId) {
        return analysisRepository.findById(analysisId);
    }

    /**
     * Get all analyses for a user.
     * @param userId the user ID
     * @return list of analyses
     */
    @Transactional(readOnly = true)
    public List<Analysis> getUserAnalyses(UUID userId) {
        return analysisRepository.findByUserUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Analysis> getUserAnalyses(UUID userId, Pageable pageable) {
        return analysisRepository.findByUserUserId(userId, pageable);
    }

    /**
     * Get all Java classes for an analysis.
     * @param analysisId the analysis ID
     * @return list of Java classes
     */
    @Transactional(readOnly = true)
    public List<JavaClass> getAnalysisClasses(UUID analysisId) {
        return javaClassRepository.findByAnalysisAnalysisId(analysisId);
    }

    /**
     * Get all methods for an analysis.
     * @param analysisId the analysis ID
     * @return list of methods
     */
    @Transactional(readOnly = true)
    public List<Method> getAnalysisMethods(UUID analysisId) {
        return methodRepository.findByAnalysisId(analysisId);
    }

    @Transactional(readOnly = true)
    public Page<Method> getAnalysisMethods(UUID analysisId, Pageable pageable) {
        return methodRepository.findByAnalysisId(analysisId, pageable);
    }

    /**
     * Get all Bob outputs for an analysis.
     * @param analysisId the analysis ID
     * @return list of Bob outputs
     */
    @Transactional(readOnly = true)
    public List<BobOutput> getAnalysisBobOutputs(UUID analysisId) {
        return bobOutputRepository.findByAnalysisId(analysisId);
    }

    /**
     * Get analysis by ID (overloaded for Long).
     * @param id the analysis ID as Long
     * @return the analysis
     */
    @Transactional(readOnly = true)
    public Analysis getAnalysisById(Long id) {
        // This method exists for compatibility but Analysis uses UUID
        throw new UnsupportedOperationException("Analysis uses UUID, not Long");
    }

    /**
     * Get analysis by ID.
     * @param id the analysis ID
     * @return the analysis
     */
    @Transactional(readOnly = true)
    public Analysis getAnalysisById(UUID id) {
        return analysisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + id));
    }

    /**
     * Get method by name within an analysis.
     * @param analysisId the analysis ID
     * @param className the class name
     * @param methodName the method name
     * @return the method
     */
    @Transactional(readOnly = true)
    public Method getMethodByName(UUID analysisId, String className, String methodName) {
        List<Method> methods = methodRepository.findByAnalysisId(analysisId);
        return methods.stream()
            .filter(m -> m.getJavaClass().getClassName().equals(className) 
                      && m.getMethodName().equals(methodName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Method not found: %s.%s in analysis %s", className, methodName, analysisId)));
    }

    /**
     * Get analysis by project name for a user.
     * @param userId the user ID
     * @param projectName the project name
     * @return the analysis
     */
    @Transactional(readOnly = true)
    public Analysis getAnalysisByProjectName(UUID userId, String projectName) {
        return analysisRepository.findByUserUserIdAndProjectName(userId, projectName)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Analysis not found for user %s with project name: %s", userId, projectName)));
    }

    /**
     * Get method by ID.
     * @param id the method ID as Long
     * @return the method
     */
    @Transactional(readOnly = true)
    public Method getMethodById(UUID id) {
        return methodRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Method not found: " + id));
    }

    /**
     * Get Bob outputs for a method with pagination.
     * @param methodId the method ID as Long
     * @param pageable the pagination info
     * @return page of Bob outputs
     */
    @Transactional(readOnly = true)
    public Page<BobOutput> getMethodBobOutputs(UUID methodId, Pageable pageable) {
        return bobOutputRepository.findAllByMethodMethodId(methodId, pageable);
    }

    /**
     * Get security scans for an analysis with pagination.
     * @param analysisId the analysis ID as Long
     * @param pageable the pagination info
     * @return page of security scans
     */
    @Transactional(readOnly = true)
    public Page<SecurityScan> getSecurityScans(UUID analysisId, Pageable pageable) {
        return securityScanRepository.findByAnalysisAnalysisId(analysisId, pageable);
    }

    /**
     * Get analysis statistics.
     * @param analysisId the analysis ID
     * @return analysis statistics
     */
    @Transactional(readOnly = true)
    public AnalysisStatistics getAnalysisStatistics(UUID analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        List<BobOutput> bobOutputs = bobOutputRepository.findByAnalysisId(analysisId);
        
        long highRiskCount = bobOutputs.stream()
            .filter(BobOutput::isHighRisk)
            .count();
        
        long methodsWithTests = bobOutputs.stream()
            .filter(bo -> Boolean.TRUE.equals(bo.getHasTests()))
            .count();
        
        double avgConfidence = bobOutputs.stream()
            .filter(bo -> bo.getConfidenceScore() != null)
            .mapToInt(BobOutput::getConfidenceScore)
            .average()
            .orElse(0.0);
        
        return new AnalysisStatistics(
            analysis.getTotalClasses(),
            analysis.getTotalMethods(),
            analysis.getAnalyzedMethods(),
            highRiskCount,
            methodsWithTests,
            avgConfidence
        );
    }

    /**
     * Delete an analysis and all associated data.
     * @param analysisId the analysis ID
     * @return true if deleted successfully
     */
    public boolean deleteAnalysis(UUID analysisId) {
        logger.warn("Deleting analysis and all associated data: {}", analysisId);
        
        if (analysisRepository.existsById(analysisId)) {
            analysisRepository.deleteById(analysisId);
            logger.info("Deleted analysis: {}", analysisId);
            return true;
        }
        
        return false;
    }

    /**
     * DTO for analysis statistics.
     */
    public static class AnalysisStatistics {
        private final Integer totalClasses;
        private final Integer totalMethods;
        private final Integer analyzedMethods;
        private final long highRiskMethods;
        private final long methodsWithTests;
        private final double averageConfidence;

        public AnalysisStatistics(Integer totalClasses, Integer totalMethods, 
                                   Integer analyzedMethods, long highRiskMethods,
                                   long methodsWithTests, double averageConfidence) {
            this.totalClasses = totalClasses;
            this.totalMethods = totalMethods;
            this.analyzedMethods = analyzedMethods;
            this.highRiskMethods = highRiskMethods;
            this.methodsWithTests = methodsWithTests;
            this.averageConfidence = averageConfidence;
        }

        // Getters
        public Integer getTotalClasses() { return totalClasses; }
        public Integer getTotalMethods() { return totalMethods; }
        public Integer getAnalyzedMethods() { return analyzedMethods; }
        public long getHighRiskMethods() { return highRiskMethods; }
        public long getMethodsWithTests() { return methodsWithTests; }
        public double getAverageConfidence() { return averageConfidence; }
    }
}

// Made with Bob
