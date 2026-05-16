package com.backend.CodeSheriff.DTO;

import com.backend.CodeSheriff.Entity.Analysis;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Analysis responses.
 * Provides a clean API response format without exposing internal entity structure.
 */
public class AnalysisResponseDTO {
    
    private UUID analysisId;
    private UUID userId;
    private String userEmail;
    private String projectName;
    private String zipFilename;
    private Integer totalClasses;
    private Integer totalMethods;
    private Integer analyzedMethods;
    private String status;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Progress percentage
    private Double progressPercentage;
    
    // Constructor
    public AnalysisResponseDTO() {
    }
    
    // Factory method to create from entity
    public static AnalysisResponseDTO fromEntity(Analysis analysis) {
        AnalysisResponseDTO dto = new AnalysisResponseDTO();
        dto.setAnalysisId(analysis.getAnalysisId());
        dto.setUserId(analysis.getUser().getUserId());
        dto.setUserEmail(analysis.getUser().getEmail());
        dto.setProjectName(analysis.getProjectName());
        dto.setZipFilename(analysis.getZipFilename());
        dto.setTotalClasses(analysis.getTotalClasses());
        dto.setTotalMethods(analysis.getTotalMethods());
        dto.setAnalyzedMethods(analysis.getAnalyzedMethods());
        dto.setStatus(analysis.getStatus());
        dto.setErrorMessage(analysis.getErrorMessage());
        dto.setStartedAt(analysis.getStartedAt());
        dto.setCompletedAt(analysis.getCompletedAt());
        dto.setCreatedAt(analysis.getCreatedAt());
        dto.setUpdatedAt(analysis.getUpdatedAt());
        
        // Calculate progress
        if (analysis.getTotalMethods() != null && analysis.getTotalMethods() > 0) {
            int analyzed = analysis.getAnalyzedMethods() != null ? analysis.getAnalyzedMethods() : 0;
            dto.setProgressPercentage((double) analyzed / analysis.getTotalMethods() * 100);
        } else {
            dto.setProgressPercentage(0.0);
        }
        
        return dto;
    }
    
    // Getters and Setters
    public UUID getAnalysisId() { return analysisId; }
    public void setAnalysisId(UUID analysisId) { this.analysisId = analysisId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getZipFilename() { return zipFilename; }
    public void setZipFilename(String zipFilename) { this.zipFilename = zipFilename; }
    
    public Integer getTotalClasses() { return totalClasses; }
    public void setTotalClasses(Integer totalClasses) { this.totalClasses = totalClasses; }
    
    public Integer getTotalMethods() { return totalMethods; }
    public void setTotalMethods(Integer totalMethods) { this.totalMethods = totalMethods; }
    
    public Integer getAnalyzedMethods() { return analyzedMethods; }
    public void setAnalyzedMethods(Integer analyzedMethods) { this.analyzedMethods = analyzedMethods; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }
}

// Made with Bob
