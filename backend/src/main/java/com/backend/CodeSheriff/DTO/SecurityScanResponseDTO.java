package com.backend.CodeSheriff.DTO;

import com.backend.CodeSheriff.Entity.SecurityScan;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for SecurityScan responses.
 * Provides security scan results in a clean API format.
 */
public class SecurityScanResponseDTO {
    
    private UUID scanId;
    private UUID analysisId;
    private String scanType;
    private String status;
    private Integer totalFlags;
    private Integer criticalFlags;
    private Integer highFlags;
    private Integer mediumFlags;
    private Integer lowFlags;
    private String scanSummary;
    private String errorMessage;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;
    private Instant createdAt;
    
    // Constructor
    public SecurityScanResponseDTO() {
    }
    
    // Factory method to create from entity
    public static SecurityScanResponseDTO fromEntity(SecurityScan scan) {
        SecurityScanResponseDTO dto = new SecurityScanResponseDTO();
        dto.setScanId(scan.getScanId());
        dto.setAnalysisId(scan.getAnalysis().getAnalysisId());
        dto.setScanType(scan.getScanType());
        dto.setStatus(scan.getStatus());
        dto.setTotalFlags(scan.getTotalFlags());
        dto.setCriticalFlags(scan.getCriticalFlags());
        dto.setHighFlags(scan.getHighFlags());
        dto.setMediumFlags(scan.getMediumFlags());
        dto.setLowFlags(scan.getLowFlags());
        dto.setScanSummary(scan.getScanSummary());
        dto.setErrorMessage(scan.getErrorMessage());
        dto.setStartedAt(scan.getStartedAt());
        dto.setCompletedAt(scan.getCompletedAt());
        dto.setDurationMs(scan.getDurationMs());
        dto.setCreatedAt(scan.getCreatedAt());
        return dto;
    }
    
    // Getters and Setters
    public UUID getScanId() { return scanId; }
    public void setScanId(UUID scanId) { this.scanId = scanId; }
    
    public UUID getAnalysisId() { return analysisId; }
    public void setAnalysisId(UUID analysisId) { this.analysisId = analysisId; }
    
    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getTotalFlags() { return totalFlags; }
    public void setTotalFlags(Integer totalFlags) { this.totalFlags = totalFlags; }
    
    public Integer getCriticalFlags() { return criticalFlags; }
    public void setCriticalFlags(Integer criticalFlags) { this.criticalFlags = criticalFlags; }
    
    public Integer getHighFlags() { return highFlags; }
    public void setHighFlags(Integer highFlags) { this.highFlags = highFlags; }
    
    public Integer getMediumFlags() { return mediumFlags; }
    public void setMediumFlags(Integer mediumFlags) { this.mediumFlags = mediumFlags; }
    
    public Integer getLowFlags() { return lowFlags; }
    public void setLowFlags(Integer lowFlags) { this.lowFlags = lowFlags; }
    
    public String getScanSummary() { return scanSummary; }
    public void setScanSummary(String scanSummary) { this.scanSummary = scanSummary; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

// Made with Bob
