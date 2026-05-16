package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Entity.Analysis;
import com.backend.CodeSheriff.Entity.SecurityFlag;
import com.backend.CodeSheriff.Entity.SecurityScan;
import com.backend.CodeSheriff.Repository.AnalysisRepository;
import com.backend.CodeSheriff.Repository.SecurityFlagRepository;
import com.backend.CodeSheriff.Repository.SecurityScanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing security scans and flags.
 * Handles creation, tracking, and reporting of security analysis results.
 */
@Service
@Transactional
public class SecurityScanService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityScanService.class);

    private final SecurityScanRepository securityScanRepository;
    private final SecurityFlagRepository securityFlagRepository;
    private final AnalysisRepository analysisRepository;

    public SecurityScanService(
            SecurityScanRepository securityScanRepository,
            SecurityFlagRepository securityFlagRepository,
            AnalysisRepository analysisRepository) {
        this.securityScanRepository = securityScanRepository;
        this.securityFlagRepository = securityFlagRepository;
        this.analysisRepository = analysisRepository;
    }

    /**
     * Create a new security scan.
     * @param analysisId the analysis ID
     * @param scanType the scan type (asi01_injection, credential_leak, hallucination_shield, full_pipeline)
     * @return the created security scan
     */
    public SecurityScan createSecurityScan(UUID analysisId, String scanType) {
        logger.info("Creating security scan for analysis {} - type: {}", analysisId, scanType);
        
        Analysis analysis = analysisRepository.findById(analysisId)
            .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));
        
        SecurityScan scan = new SecurityScan(analysis, scanType);
        scan.setStatus("pending");
        
        SecurityScan saved = securityScanRepository.save(scan);
        logger.info("Created security scan: {} for analysis: {}", saved.getScanId(), analysisId);
        
        return saved;
    }

    /**
     * Start a security scan.
     * @param scanId the scan ID
     * @return the updated security scan
     */
    public SecurityScan startScan(UUID scanId) {
        logger.info("Starting security scan: {}", scanId);
        
        SecurityScan scan = securityScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Security scan not found: " + scanId));
        
        scan.markAsStarted();
        SecurityScan updated = securityScanRepository.save(scan);
        
        logger.info("Security scan started: {}", scanId);
        return updated;
    }

    /**
     * Complete a security scan successfully.
     * @param scanId the scan ID
     * @param summary the scan summary
     * @return the updated security scan
     */
    public SecurityScan completeScan(UUID scanId, String summary) {
        logger.info("Completing security scan: {}", scanId);
        
        SecurityScan scan = securityScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Security scan not found: " + scanId));
        
        scan.setScanSummary(summary);
        scan.markAsCompleted();
        SecurityScan updated = securityScanRepository.save(scan);
        
        logger.info("Security scan completed: {} - {} total flags ({} critical)", 
                   scanId, scan.getTotalFlags(), scan.getCriticalFlags());
        return updated;
    }

    /**
     * Mark a security scan as failed.
     * @param scanId the scan ID
     * @param errorMessage the error message
     * @return the updated security scan
     */
    public SecurityScan failScan(UUID scanId, String errorMessage) {
        logger.error("Failing security scan: {} - {}", scanId, errorMessage);
        
        SecurityScan scan = securityScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Security scan not found: " + scanId));
        
        scan.markAsFailed(errorMessage);
        SecurityScan updated = securityScanRepository.save(scan);
        
        logger.info("Security scan failed: {}", scanId);
        return updated;
    }

    /**
     * Add a security flag to a scan.
     * @param scanId the scan ID
     * @param flag the security flag
     * @return the saved security flag
     */
    public SecurityFlag addSecurityFlag(UUID scanId, SecurityFlag flag) {
        logger.debug("Adding security flag to scan: {} - {}", scanId, flag.getTitle());
        
        SecurityScan scan = securityScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Security scan not found: " + scanId));
        
        flag.setSecurityScan(scan);
        SecurityFlag saved = securityFlagRepository.save(flag);
        
        // Update scan counters (handled by entity helper methods)
        scan.addSecurityFlag(saved);
        securityScanRepository.save(scan);
        
        logger.debug("Added security flag: {} to scan: {}", saved.getFlagId(), scanId);
        return saved;
    }

    /**
     * Create and add a security flag in one operation.
     * @param scanId the scan ID
     * @param flagType the flag type
     * @param severity the severity
     * @param title the title
     * @param description the description
     * @param filePath the file path
     * @param className the class name
     * @param methodName the method name
     * @param lineNumber the line number
     * @param codeSnippet the code snippet
     * @param recommendation the recommendation
     * @return the created security flag
     */
    public SecurityFlag createSecurityFlag(
            UUID scanId, String flagType, String severity, String title,
            String description, String filePath, String className, String methodName,
            Integer lineNumber, String codeSnippet, String recommendation) {
        
        SecurityScan scan = securityScanRepository.findById(scanId)
            .orElseThrow(() -> new IllegalArgumentException("Security scan not found: " + scanId));
        
        SecurityFlag flag = new SecurityFlag(scan, flagType, severity, title);
        flag.setDescription(description);
        flag.setFilePath(filePath);
        flag.setClassName(className);
        flag.setMethodName(methodName);
        flag.setLineNumber(lineNumber);
        flag.setCodeSnippet(codeSnippet);
        flag.setRecommendation(recommendation);
        
        return addSecurityFlag(scanId, flag);
    }

    /**
     * Resolve a security flag.
     * @param flagId the flag ID
     * @param resolutionNotes the resolution notes
     * @return the updated security flag
     */
    public SecurityFlag resolveFlag(UUID flagId, String resolutionNotes) {
        logger.info("Resolving security flag: {}", flagId);
        
        SecurityFlag flag = securityFlagRepository.findById(flagId)
            .orElseThrow(() -> new IllegalArgumentException("Security flag not found: " + flagId));
        
        flag.markAsResolved(resolutionNotes);
        SecurityFlag updated = securityFlagRepository.save(flag);
        
        logger.info("Security flag resolved: {}", flagId);
        return updated;
    }

    /**
     * Mark a security flag as false positive.
     * @param flagId the flag ID
     * @param notes the notes
     * @return the updated security flag
     */
    public SecurityFlag markAsFalsePositive(UUID flagId, String notes) {
        logger.info("Marking security flag as false positive: {}", flagId);
        
        SecurityFlag flag = securityFlagRepository.findById(flagId)
            .orElseThrow(() -> new IllegalArgumentException("Security flag not found: " + flagId));
        
        flag.markAsFalsePositive(notes);
        SecurityFlag updated = securityFlagRepository.save(flag);
        
        logger.info("Security flag marked as false positive: {}", flagId);
        return updated;
    }

    /**
     * Get security scan by ID.
     * @param scanId the scan ID
     * @return Optional containing the scan if found
     */
    @Transactional(readOnly = true)
    public Optional<SecurityScan> getSecurityScan(UUID scanId) {
        return securityScanRepository.findById(scanId);
    }

    /**
     * Get all security scans for an analysis.
     * @param analysisId the analysis ID
     * @return list of security scans
     */
    @Transactional(readOnly = true)
    public List<SecurityScan> getAnalysisScans(UUID analysisId) {
        return securityScanRepository.findByAnalysisAnalysisId(analysisId);
    }

    /**
     * Get all security flags for a scan.
     * @param scanId the scan ID
     * @return list of security flags
     */
    @Transactional(readOnly = true)
    public List<SecurityFlag> getScanFlags(UUID scanId) {
        return securityFlagRepository.findBySecurityScanScanId(scanId);
    }

    /**
     * Get unresolved high-priority flags for an analysis.
     * @param analysisId the analysis ID
     * @return list of high-priority security flags
     */
    @Transactional(readOnly = true)
    public List<SecurityFlag> getHighPriorityFlags(UUID analysisId) {
        return securityFlagRepository.findByAnalysisId(analysisId).stream()
            .filter(flag -> !Boolean.TRUE.equals(flag.getIsResolved()))
            .filter(flag -> "critical".equalsIgnoreCase(flag.getSeverity()) || 
                           "high".equalsIgnoreCase(flag.getSeverity()))
            .toList();
    }

    /**
     * Get security scan statistics for an analysis.
     * @param analysisId the analysis ID
     * @return security scan statistics
     */
    @Transactional(readOnly = true)
    public SecurityScanStatistics getSecurityStatistics(UUID analysisId) {
        List<SecurityScan> scans = securityScanRepository.findByAnalysisAnalysisId(analysisId);
        List<SecurityFlag> flags = securityFlagRepository.findByAnalysisId(analysisId);
        
        int totalScans = scans.size();
        int completedScans = (int) scans.stream()
            .filter(scan -> "completed".equals(scan.getStatus()))
            .count();
        
        int totalFlags = flags.size();
        int criticalFlags = (int) flags.stream()
            .filter(flag -> "critical".equalsIgnoreCase(flag.getSeverity()))
            .count();
        int highFlags = (int) flags.stream()
            .filter(flag -> "high".equalsIgnoreCase(flag.getSeverity()))
            .count();
        int unresolvedFlags = (int) flags.stream()
            .filter(flag -> !Boolean.TRUE.equals(flag.getIsResolved()))
            .count();
        
        return new SecurityScanStatistics(
            totalScans, completedScans, totalFlags, 
            criticalFlags, highFlags, unresolvedFlags
        );
    }

    /**
     * Delete a security scan and all associated flags.
     * @param scanId the scan ID
     * @return true if deleted successfully
     */
    public boolean deleteSecurityScan(UUID scanId) {
        logger.warn("Deleting security scan and all associated flags: {}", scanId);
        
        if (securityScanRepository.existsById(scanId)) {
            securityScanRepository.deleteById(scanId);
            logger.info("Deleted security scan: {}", scanId);
            return true;
        }
        
        return false;
    }

    /**
     * DTO for security scan statistics.
     */
    public static class SecurityScanStatistics {
        private final int totalScans;
        private final int completedScans;
        private final int totalFlags;
        private final int criticalFlags;
        private final int highFlags;
        private final int unresolvedFlags;

        public SecurityScanStatistics(int totalScans, int completedScans, int totalFlags,
                                       int criticalFlags, int highFlags, int unresolvedFlags) {
            this.totalScans = totalScans;
            this.completedScans = completedScans;
            this.totalFlags = totalFlags;
            this.criticalFlags = criticalFlags;
            this.highFlags = highFlags;
            this.unresolvedFlags = unresolvedFlags;
        }

        // Getters
        public int getTotalScans() { return totalScans; }
        public int getCompletedScans() { return completedScans; }
        public int getTotalFlags() { return totalFlags; }
        public int getCriticalFlags() { return criticalFlags; }
        public int getHighFlags() { return highFlags; }
        public int getUnresolvedFlags() { return unresolvedFlags; }
    }
}

// Made with Bob
