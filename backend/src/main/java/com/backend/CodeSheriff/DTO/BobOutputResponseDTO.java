package com.backend.CodeSheriff.DTO;

import com.backend.CodeSheriff.Entity.BobOutput;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for BobOutput responses.
 * Provides Bob AI analysis results in a clean API format.
 */
public class BobOutputResponseDTO {
    
    private UUID bobOutputId;
    private UUID methodId;
    private String methodName;
    private String className;
    private String summary;
    private String purpose;
    private String riskLevel;
    private String riskExplanation;
    private String securityConcerns;
    private String codeSmells;
    private String suggestions;
    private String bestPractices;
    private Integer confidenceScore;
    private Boolean hasTests;
    private Integer testCoverageEstimate;
    private String complexityAssessment;
    private Integer maintainabilityScore;
    private String performanceNotes;
    private String dependencies;
    private String modelUsed;
    private Integer tokensUsed;
    private Long processingTimeMs;
    private Instant createdAt;
    
    // Additional fields for controllers
    private Integer securityFlagCount;
    private Integer criticalFlagCount;
    private UUID securityScanId;
    
    // Constructor
    public BobOutputResponseDTO() {
    }
    
    // Factory method to create from entity
    public static BobOutputResponseDTO fromEntity(BobOutput bobOutput) {
        BobOutputResponseDTO dto = new BobOutputResponseDTO();
        dto.setBobOutputId(bobOutput.getBobOutputId());
        dto.setMethodId(bobOutput.getMethod().getMethodId());
        dto.setMethodName(bobOutput.getMethod().getMethodName());
        dto.setClassName(bobOutput.getMethod().getJavaClass().getClassName());
        dto.setSummary(bobOutput.getSummary());
        dto.setPurpose(bobOutput.getPurpose());
        dto.setRiskLevel(bobOutput.getRiskLevel());
        dto.setRiskExplanation(bobOutput.getRiskExplanation());
        dto.setSecurityConcerns(bobOutput.getSecurityConcerns());
        dto.setCodeSmells(bobOutput.getCodeSmells());
        dto.setSuggestions(bobOutput.getSuggestions());
        dto.setBestPractices(bobOutput.getBestPractices());
        dto.setConfidenceScore(bobOutput.getConfidenceScore());
        dto.setHasTests(bobOutput.getHasTests());
        dto.setTestCoverageEstimate(bobOutput.getTestCoverageEstimate());
        dto.setComplexityAssessment(bobOutput.getComplexityAssessment());
        dto.setMaintainabilityScore(bobOutput.getMaintainabilityScore());
        dto.setPerformanceNotes(bobOutput.getPerformanceNotes());
        dto.setDependencies(bobOutput.getDependencies());
        dto.setModelUsed(bobOutput.getModelUsed());
        dto.setTokensUsed(bobOutput.getTokensUsed());
        dto.setProcessingTimeMs(bobOutput.getProcessingTimeMs());
        dto.setCreatedAt(bobOutput.getCreatedAt());
        dto.setSecurityFlagCount(0); // Default, can be set by caller
        dto.setCriticalFlagCount(0); // Default, can be set by caller
        return dto;
    }
    
    // Getters and Setters
    public UUID getBobOutputId() { return bobOutputId; }
    public void setBobOutputId(UUID bobOutputId) { this.bobOutputId = bobOutputId; }
    
    public UUID getMethodId() { return methodId; }
    public void setMethodId(UUID methodId) { this.methodId = methodId; }
    
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    
    public String getRiskExplanation() { return riskExplanation; }
    public void setRiskExplanation(String riskExplanation) { this.riskExplanation = riskExplanation; }
    
    public String getSecurityConcerns() { return securityConcerns; }
    public void setSecurityConcerns(String securityConcerns) { this.securityConcerns = securityConcerns; }
    
    public String getCodeSmells() { return codeSmells; }
    public void setCodeSmells(String codeSmells) { this.codeSmells = codeSmells; }
    
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    
    public String getBestPractices() { return bestPractices; }
    public void setBestPractices(String bestPractices) { this.bestPractices = bestPractices; }
    
    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public Boolean getHasTests() { return hasTests; }
    public void setHasTests(Boolean hasTests) { this.hasTests = hasTests; }
    
    public Integer getTestCoverageEstimate() { return testCoverageEstimate; }
    public void setTestCoverageEstimate(Integer testCoverageEstimate) { this.testCoverageEstimate = testCoverageEstimate; }
    
    public String getComplexityAssessment() { return complexityAssessment; }
    public void setComplexityAssessment(String complexityAssessment) { this.complexityAssessment = complexityAssessment; }
    
    public Integer getMaintainabilityScore() { return maintainabilityScore; }
    public void setMaintainabilityScore(Integer maintainabilityScore) { this.maintainabilityScore = maintainabilityScore; }
    
    public String getPerformanceNotes() { return performanceNotes; }
    public void setPerformanceNotes(String performanceNotes) { this.performanceNotes = performanceNotes; }
    
    public String getDependencies() { return dependencies; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }
    
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Integer getSecurityFlagCount() { return securityFlagCount; }
    public void setSecurityFlagCount(Integer securityFlagCount) { this.securityFlagCount = securityFlagCount; }
    
    public Integer getCriticalFlagCount() { return criticalFlagCount; }
    public void setCriticalFlagCount(Integer criticalFlagCount) { this.criticalFlagCount = criticalFlagCount; }

    public UUID getSecurityScanId() { return securityScanId; }
    public void setSecurityScanId(UUID securityScanId) { this.securityScanId = securityScanId; }
}

// Made with Bob
