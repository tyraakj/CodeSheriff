package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SecurityScan entity representing a security analysis session.
 * Maps to the 'security_scans' table in Supabase PostgreSQL.
 * 
 * Each SecurityScan belongs to an Analysis and contains multiple SecurityFlags
 * representing detected security issues.
 */
@Entity
@Table(name = "security_scans", schema = "public", indexes = {
    @Index(name = "idx_security_scans_analysis_id", columnList = "analysis_id"),
    @Index(name = "idx_security_scans_scan_type", columnList = "scan_type"),
    @Index(name = "idx_security_scans_status", columnList = "status"),
    @Index(name = "idx_security_scans_created_at", columnList = "created_at")
})
public class SecurityScan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "scan_id", nullable = false, updatable = false)
    private UUID scanId;

    @NotNull(message = "Analysis is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, foreignKey = @ForeignKey(name = "fk_security_scans_analysis"))
    private Analysis analysis;

    @NotBlank(message = "Scan type is required")
    @Size(max = 100, message = "Scan type must not exceed 100 characters")
    @Column(name = "scan_type", nullable = false, length = 100)
    private String scanType; // asi01_injection, credential_leak, hallucination_shield, full_pipeline

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Column(name = "status", nullable = false, length = 50)
    private String status = "pending"; // pending, running, completed, failed

    @Column(name = "total_flags")
    private Integer totalFlags = 0;

    @Column(name = "critical_flags")
    private Integer criticalFlags = 0;

    @Column(name = "high_flags")
    private Integer highFlags = 0;

    @Column(name = "medium_flags")
    private Integer mediumFlags = 0;

    @Column(name = "low_flags")
    private Integer lowFlags = 0;

    @Column(name = "scan_summary", columnDefinition = "TEXT")
    private String scanSummary;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "securityScan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SecurityFlag> securityFlags = new ArrayList<>();

    // Constructors
    public SecurityScan() {
    }

    public SecurityScan(Analysis analysis, String scanType) {
        this.analysis = analysis;
        this.scanType = scanType;
    }

    // Getters and Setters
    public UUID getScanId() {
        return scanId;
    }

    public void setScanId(UUID scanId) {
        this.scanId = scanId;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalFlags() {
        return totalFlags;
    }

    public void setTotalFlags(Integer totalFlags) {
        this.totalFlags = totalFlags;
    }

    public Integer getCriticalFlags() {
        return criticalFlags;
    }

    public void setCriticalFlags(Integer criticalFlags) {
        this.criticalFlags = criticalFlags;
    }

    public Integer getHighFlags() {
        return highFlags;
    }

    public void setHighFlags(Integer highFlags) {
        this.highFlags = highFlags;
    }

    public Integer getMediumFlags() {
        return mediumFlags;
    }

    public void setMediumFlags(Integer mediumFlags) {
        this.mediumFlags = mediumFlags;
    }

    public Integer getLowFlags() {
        return lowFlags;
    }

    public void setLowFlags(Integer lowFlags) {
        this.lowFlags = lowFlags;
    }

    public String getScanSummary() {
        return scanSummary;
    }

    public void setScanSummary(String scanSummary) {
        this.scanSummary = scanSummary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<SecurityFlag> getSecurityFlags() {
        return securityFlags;
    }

    public void setSecurityFlags(List<SecurityFlag> securityFlags) {
        this.securityFlags = securityFlags;
    }

    // Helper methods for bidirectional relationships
    public void addSecurityFlag(SecurityFlag securityFlag) {
        securityFlags.add(securityFlag);
        securityFlag.setSecurityScan(this);
        incrementFlagCount(securityFlag.getSeverity());
    }

    public void removeSecurityFlag(SecurityFlag securityFlag) {
        securityFlags.remove(securityFlag);
        securityFlag.setSecurityScan(null);
        decrementFlagCount(securityFlag.getSeverity());
    }

    // Business logic helpers
    public void markAsStarted() {
        this.status = "running";
        this.startedAt = Instant.now();
    }

    public void markAsCompleted() {
        this.status = "completed";
        this.completedAt = Instant.now();
        if (startedAt != null) {
            this.durationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = "failed";
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        if (startedAt != null) {
            this.durationMs = completedAt.toEpochMilli() - startedAt.toEpochMilli();
        }
    }

    private void incrementFlagCount(String severity) {
        this.totalFlags = (this.totalFlags == null ? 0 : this.totalFlags) + 1;
        
        if (severity == null) return;
        
        switch (severity.toLowerCase()) {
            case "critical":
                this.criticalFlags = (this.criticalFlags == null ? 0 : this.criticalFlags) + 1;
                break;
            case "high":
                this.highFlags = (this.highFlags == null ? 0 : this.highFlags) + 1;
                break;
            case "medium":
                this.mediumFlags = (this.mediumFlags == null ? 0 : this.mediumFlags) + 1;
                break;
            case "low":
                this.lowFlags = (this.lowFlags == null ? 0 : this.lowFlags) + 1;
                break;
        }
    }

    private void decrementFlagCount(String severity) {
        this.totalFlags = Math.max(0, (this.totalFlags == null ? 0 : this.totalFlags) - 1);
        
        if (severity == null) return;
        
        switch (severity.toLowerCase()) {
            case "critical":
                this.criticalFlags = Math.max(0, (this.criticalFlags == null ? 0 : this.criticalFlags) - 1);
                break;
            case "high":
                this.highFlags = Math.max(0, (this.highFlags == null ? 0 : this.highFlags) - 1);
                break;
            case "medium":
                this.mediumFlags = Math.max(0, (this.mediumFlags == null ? 0 : this.mediumFlags) - 1);
                break;
            case "low":
                this.lowFlags = Math.max(0, (this.lowFlags == null ? 0 : this.lowFlags) - 1);
                break;
        }
    }

    public boolean hasCriticalIssues() {
        return criticalFlags != null && criticalFlags > 0;
    }

    public boolean hasHighSeverityIssues() {
        return (criticalFlags != null && criticalFlags > 0) || 
               (highFlags != null && highFlags > 0);
    }

    public boolean isPassed() {
        return "completed".equals(status) && !hasCriticalIssues();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityScan)) return false;
        SecurityScan that = (SecurityScan) o;
        return scanId != null && scanId.equals(that.getScanId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SecurityScan{" +
                "scanId=" + scanId +
                ", scanType='" + scanType + '\'' +
                ", status='" + status + '\'' +
                ", totalFlags=" + totalFlags +
                ", criticalFlags=" + criticalFlags +
                ", highFlags=" + highFlags +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
