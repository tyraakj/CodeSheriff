package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Entity.*;
import com.backend.CodeSheriff.Security.ASI01InjectionDetector;
import com.backend.CodeSheriff.Security.CredentialLeakDetector;
import com.backend.CodeSheriff.Security.HallucinationShield;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Security Pipeline Service - Orchestrates all 4 security layers.
 * 
 * Pipeline Flow:
 * 1. Layer 1: ASI01 Injection Detection (scan comments/strings for prompt injection)
 * 2. Layer 2: Credential Leak Detection (scan for hardcoded secrets)
 * 3. Layer 3: Hallucination Shield (validate Bob outputs against AST)
 * 4. Layer 4: Audit Trail (log all security findings)
 * 
 * This service coordinates the execution of all layers and aggregates results.
 */
@Service
@Transactional
public class SecurityPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityPipelineService.class);

    private final SecurityScanService securityScanService;
    private final AuditTrailService auditTrailService;
    private final ASI01InjectionDetector asi01Detector;
    private final CredentialLeakDetector credentialDetector;
    private final HallucinationShield hallucinationShield;
    private final JavaParser javaParser;

    public SecurityPipelineService(
            SecurityScanService securityScanService,
            AuditTrailService auditTrailService,
            ASI01InjectionDetector asi01Detector,
            CredentialLeakDetector credentialDetector,
            HallucinationShield hallucinationShield) {
        this.securityScanService = securityScanService;
        this.auditTrailService = auditTrailService;
        this.asi01Detector = asi01Detector;
        this.credentialDetector = credentialDetector;
        this.hallucinationShield = hallucinationShield;
        this.javaParser = new JavaParser();
    }

    /**
     * Run the complete 4-layer security pipeline on an analysis.
     * @param analysis the analysis to scan
     * @param user the user running the scan
     * @param request the HTTP request (for audit logging)
     * @return the pipeline result
     */
    public SecurityPipelineResult runFullPipeline(Analysis analysis, User user, HttpServletRequest request) {
        logger.info("Starting full security pipeline for analysis: {}", analysis.getAnalysisId());
        
        Instant startTime = Instant.now();
        SecurityPipelineResult result = new SecurityPipelineResult(analysis.getAnalysisId());
        
        try {
            // Create master security scan
            SecurityScan masterScan = securityScanService.createSecurityScan(
                analysis.getAnalysisId(), 
                "full_pipeline"
            );
            securityScanService.startScan(masterScan.getScanId());
            result.setMasterScanId(masterScan.getScanId());
            
            // Layer 1: ASI01 Injection Detection
            logger.info("Running Layer 1: ASI01 Injection Detection");
            SecurityScan layer1Scan = runLayer1(analysis, masterScan);
            result.setLayer1ScanId(layer1Scan.getScanId());
            result.setLayer1Flags(layer1Scan.getTotalFlags());
            
            // Layer 2: Credential Leak Detection
            logger.info("Running Layer 2: Credential Leak Detection");
            SecurityScan layer2Scan = runLayer2(analysis, masterScan);
            result.setLayer2ScanId(layer2Scan.getScanId());
            result.setLayer2Flags(layer2Scan.getTotalFlags());
            
            // Layer 3: Hallucination Shield (if Bob outputs exist)
            logger.info("Running Layer 3: Hallucination Shield");
            SecurityScan layer3Scan = runLayer3(analysis, masterScan);
            result.setLayer3ScanId(layer3Scan.getScanId());
            result.setLayer3Flags(layer3Scan.getTotalFlags());
            
            // Calculate totals
            int totalFlags = layer1Scan.getTotalFlags() + layer2Scan.getTotalFlags() + layer3Scan.getTotalFlags();
            int criticalFlags = layer1Scan.getCriticalFlags() + layer2Scan.getCriticalFlags() + layer3Scan.getCriticalFlags();
            
            // Complete master scan
            String summary = String.format(
                "Full pipeline completed. Layer 1: %d flags, Layer 2: %d flags, Layer 3: %d flags. Total: %d flags (%d critical).",
                layer1Scan.getTotalFlags(), layer2Scan.getTotalFlags(), layer3Scan.getTotalFlags(),
                totalFlags, criticalFlags
            );
            securityScanService.completeScan(masterScan.getScanId(), summary);
            
            result.setSuccess(true);
            result.setTotalFlags(totalFlags);
            result.setCriticalFlags(criticalFlags);
            
            // Layer 4: Audit Trail
            long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            auditTrailService.logSecurityScan(user, "full_pipeline", masterScan.getScanId(), 
                                              totalFlags, criticalFlags, request);
            
            logger.info("Security pipeline completed for analysis {} - {} flags ({} critical) in {}ms",
                       analysis.getAnalysisId(), totalFlags, criticalFlags, duration);
            
        } catch (Exception e) {
            logger.error("Security pipeline failed for analysis {}: {}", 
                        analysis.getAnalysisId(), e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    /**
     * Run Layer 1: ASI01 Injection Detection.
     */
    private SecurityScan runLayer1(Analysis analysis, SecurityScan masterScan) {
        SecurityScan layer1Scan = securityScanService.createSecurityScan(
            analysis.getAnalysisId(), 
            "asi01_injection"
        );
        securityScanService.startScan(layer1Scan.getScanId());
        
        try {
            List<JavaClass> classes = analysis.getJavaClasses();
            
            for (JavaClass javaClass : classes) {
                if (javaClass.getSourceCode() != null) {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(javaClass.getSourceCode());
                    
                    if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                        CompilationUnit cu = parseResult.getResult().get();
                        
                        // Scan class
                        List<SecurityFlag> classFlags = asi01Detector.scanJavaClass(javaClass, cu, layer1Scan);
                        classFlags.forEach(flag -> securityScanService.addSecurityFlag(layer1Scan.getScanId(), flag));
                        
                        // Scan methods
                        for (Method method : javaClass.getMethods()) {
                            cu.findAll(MethodDeclaration.class).stream()
                                .filter(md -> md.getNameAsString().equals(method.getMethodName()))
                                .findFirst()
                                .ifPresent(methodDecl -> {
                                    List<SecurityFlag> methodFlags = asi01Detector.scanMethod(
                                        method, methodDecl, javaClass, layer1Scan
                                    );
                                    methodFlags.forEach(flag -> 
                                        securityScanService.addSecurityFlag(layer1Scan.getScanId(), flag)
                                    );
                                });
                        }
                    }
                }
            }
            
            securityScanService.completeScan(layer1Scan.getScanId(), 
                "ASI01 injection detection completed");
            
        } catch (Exception e) {
            logger.error("Layer 1 failed: {}", e.getMessage(), e);
            securityScanService.failScan(layer1Scan.getScanId(), e.getMessage());
        }
        
        return securityScanService.getSecurityScan(layer1Scan.getScanId()).orElse(layer1Scan);
    }

    /**
     * Run Layer 2: Credential Leak Detection.
     */
    private SecurityScan runLayer2(Analysis analysis, SecurityScan masterScan) {
        SecurityScan layer2Scan = securityScanService.createSecurityScan(
            analysis.getAnalysisId(), 
            "credential_leak"
        );
        securityScanService.startScan(layer2Scan.getScanId());
        
        try {
            List<JavaClass> classes = analysis.getJavaClasses();
            
            for (JavaClass javaClass : classes) {
                if (javaClass.getSourceCode() != null) {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(javaClass.getSourceCode());
                    
                    if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                        CompilationUnit cu = parseResult.getResult().get();
                        
                        // Scan class
                        List<SecurityFlag> classFlags = credentialDetector.scanJavaClass(javaClass, cu, layer2Scan);
                        classFlags.forEach(flag -> securityScanService.addSecurityFlag(layer2Scan.getScanId(), flag));
                        
                        // Scan methods
                        for (Method method : javaClass.getMethods()) {
                            cu.findAll(MethodDeclaration.class).stream()
                                .filter(md -> md.getNameAsString().equals(method.getMethodName()))
                                .findFirst()
                                .ifPresent(methodDecl -> {
                                    List<SecurityFlag> methodFlags = credentialDetector.scanMethod(
                                        method, methodDecl, javaClass, layer2Scan
                                    );
                                    methodFlags.forEach(flag -> 
                                        securityScanService.addSecurityFlag(layer2Scan.getScanId(), flag)
                                    );
                                });
                        }
                    }
                }
            }
            
            securityScanService.completeScan(layer2Scan.getScanId(), 
                "Credential leak detection completed");
            
        } catch (Exception e) {
            logger.error("Layer 2 failed: {}", e.getMessage(), e);
            securityScanService.failScan(layer2Scan.getScanId(), e.getMessage());
        }
        
        return securityScanService.getSecurityScan(layer2Scan.getScanId()).orElse(layer2Scan);
    }

    /**
     * Run Layer 3: Hallucination Shield.
     */
    private SecurityScan runLayer3(Analysis analysis, SecurityScan masterScan) {
        SecurityScan layer3Scan = securityScanService.createSecurityScan(
            analysis.getAnalysisId(), 
            "hallucination_shield"
        );
        securityScanService.startScan(layer3Scan.getScanId());
        
        try {
            List<JavaClass> classes = analysis.getJavaClasses();
            int validatedCount = 0;
            
            for (JavaClass javaClass : classes) {
                if (javaClass.getSourceCode() != null) {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(javaClass.getSourceCode());
                    
                    if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                        CompilationUnit cu = parseResult.getResult().get();
                        
                        // Validate methods with Bob outputs
                        for (Method method : javaClass.getMethods()) {
                            if (method.getBobOutput() != null) {
                                cu.findAll(MethodDeclaration.class).stream()
                                    .filter(md -> md.getNameAsString().equals(method.getMethodName()))
                                    .findFirst()
                                    .ifPresent(methodDecl -> {
                                        // Capture baseline
                                        HallucinationShield.ASTBaseline baseline = 
                                            hallucinationShield.captureBaseline(methodDecl, method.getSourceCode());
                                        
                                        // Validate Bob output
                                        List<SecurityFlag> flags = hallucinationShield.validateBobOutput(
                                            method.getBobOutput(), baseline, method, javaClass, layer3Scan
                                        );
                                        
                                        flags.forEach(flag -> 
                                            securityScanService.addSecurityFlag(layer3Scan.getScanId(), flag)
                                        );
                                    });
                                validatedCount++;
                            }
                        }
                    }
                }
            }
            
            securityScanService.completeScan(layer3Scan.getScanId(), 
                String.format("Hallucination shield validated %d Bob outputs", validatedCount));
            
        } catch (Exception e) {
            logger.error("Layer 3 failed: {}", e.getMessage(), e);
            securityScanService.failScan(layer3Scan.getScanId(), e.getMessage());
        }
        
        return securityScanService.getSecurityScan(layer3Scan.getScanId()).orElse(layer3Scan);
    }

    /**
     * Run a single layer of the pipeline.
     * @param analysis the analysis
     * @param layerNumber the layer number (1-3)
     * @param user the user
     * @param request the HTTP request
     * @return the security scan result
     */
    public SecurityScan runSingleLayer(Analysis analysis, int layerNumber, User user, HttpServletRequest request) {
        logger.info("Running security layer {} for analysis: {}", layerNumber, analysis.getAnalysisId());
        
        SecurityScan scan = switch (layerNumber) {
            case 1 -> runLayer1(analysis, null);
            case 2 -> runLayer2(analysis, null);
            case 3 -> runLayer3(analysis, null);
            default -> throw new IllegalArgumentException("Invalid layer number: " + layerNumber);
        };
        
        // Log to audit trail
        auditTrailService.logSecurityScan(user, "layer_" + layerNumber, scan.getScanId(),
                                          scan.getTotalFlags(), scan.getCriticalFlags(), request);
        
        return scan;
    }

    /**
     * Result of security pipeline execution.
     */
    public static class SecurityPipelineResult {
        private final UUID analysisId;
        private UUID masterScanId;
        private UUID layer1ScanId;
        private UUID layer2ScanId;
        private UUID layer3ScanId;
        private int layer1Flags;
        private int layer2Flags;
        private int layer3Flags;
        private int totalFlags;
        private int criticalFlags;
        private boolean success;
        private String errorMessage;

        public SecurityPipelineResult(UUID analysisId) {
            this.analysisId = analysisId;
        }

        // Getters and setters
        public UUID getAnalysisId() { return analysisId; }
        public UUID getMasterScanId() { return masterScanId; }
        public void setMasterScanId(UUID masterScanId) { this.masterScanId = masterScanId; }
        public UUID getLayer1ScanId() { return layer1ScanId; }
        public void setLayer1ScanId(UUID layer1ScanId) { this.layer1ScanId = layer1ScanId; }
        public UUID getLayer2ScanId() { return layer2ScanId; }
        public void setLayer2ScanId(UUID layer2ScanId) { this.layer2ScanId = layer2ScanId; }
        public UUID getLayer3ScanId() { return layer3ScanId; }
        public void setLayer3ScanId(UUID layer3ScanId) { this.layer3ScanId = layer3ScanId; }
        public int getLayer1Flags() { return layer1Flags; }
        public void setLayer1Flags(int layer1Flags) { this.layer1Flags = layer1Flags; }
        public int getLayer2Flags() { return layer2Flags; }
        public void setLayer2Flags(int layer2Flags) { this.layer2Flags = layer2Flags; }
        public int getLayer3Flags() { return layer3Flags; }
        public void setLayer3Flags(int layer3Flags) { this.layer3Flags = layer3Flags; }
        public int getTotalFlags() { return totalFlags; }
        public void setTotalFlags(int totalFlags) { this.totalFlags = totalFlags; }
        public int getCriticalFlags() { return criticalFlags; }
        public void setCriticalFlags(int criticalFlags) { this.criticalFlags = criticalFlags; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public boolean hasCriticalIssues() {
            return criticalFlags > 0;
        }

        public boolean isPassed() {
            return success && !hasCriticalIssues();
        }

        public SecurityScan getSecurityScan() {
            // This method returns the master scan if available
            // Callers should use securityScanService.getSecurityScan(masterScanId) instead
            return null;
        }
    }
}

// Made with Bob
