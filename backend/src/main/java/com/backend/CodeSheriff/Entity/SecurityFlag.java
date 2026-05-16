package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * SecurityFlag entity representing an individual security issue detected during a scan.
 * Maps to the 'security_flags' table in Supabase PostgreSQL.
 * 
 * Each SecurityFlag belongs to a SecurityScan and represents a specific
 * security concern found in the code (e.g., SQL injection, hardcoded credentials).
 */
@Entity
@Table(name = "security_flags", schema = "public", indexes = {
    @Index(name = "idx_security_flags_scan_id", columnList = "scan_id"),
    @Index(name = "idx_security_flags_severity", columnList = "severity"),
    @Index(name = "idx_security_flags_flag_type", columnList = "flag_type"),
    @Index(name = "idx_security_flags_is_resolved", columnList = "is_resolved")
})
public class SecurityFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flag_id", nullable = false, updatable = false)
    private UUID flagId;

    @NotNull(message = "Security scan is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_security_flags_scan"))
    private SecurityScan securityScan;

    @NotBlank(message = "Flag type is required")
    @Size(max = 100, message = "Flag type must not exceed 100 characters")
    @Column(name = "flag_type", nullable = false, length = 100)
    private String flagType; // asi01_injection, sql_injection, xss, hardcoded_credential, etc.

    @NotBlank(message = "Severity is required")
    @Size(max = 50, message = "Severity must not exceed 50 characters")
    @Column(name = "severity", nullable = false, length = 50)
    private String severity; // critical, high, medium, low

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 500, message = "File path must not exceed 500 characters")
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Size(max = 255, message = "Class name must not exceed 255 characters")
    @Column(name = "class_name", length = 255)
    private String className;

    @Size(max = 255, message = "Method name must not exceed 255 characters")
    @Column(name = "method_name", length = 255)
    private String methodName;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "cwe_id", length = 50)
    private String cweId; // Common Weakness Enumeration ID (e.g., "CWE-89" for SQL Injection)

    @Column(name = "owasp_category", length = 100)
    private String owaspCategory; // OWASP Top 10 category

    @Column(name = "confidence_score")
    private Integer confidenceScore; // 0-100, how confident the detector is

    @Column(name = "is_false_positive")
    private Boolean isFalsePositive = false;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Size(max = 500, message = "Resolution notes must not exceed 500 characters")
    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;

    @Column(name = "detection_metadata", columnDefinition = "TEXT")
    private String detectionMetadata; // JSON string with additional detection details

    // Additional fields for DTOs and controllers
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Column(name = "false_positive")
    private Boolean falsePositive = false;

    @Column(name = "false_positive_reason", columnDefinition = "TEXT")
    private String falsePositiveReason;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", foreignKey = @ForeignKey(name = "fk_security_flags_method"))
    private Method method;

    @Column(name = "java_class_name", length = 255)
    private String javaClassName;

    // Constructors
    public SecurityFlag() {
    }

    public SecurityFlag(SecurityScan securityScan, String flagType, String severity, String title) {
        this.securityScan = securityScan;
        this.flagType = flagType;
        this.severity = severity;
        this.title = title;
    }

    // Getters and Setters
    public UUID getFlagId() {
        return flagId;
    }

    public void setFlagId(UUID flagId) {
        this.flagId = flagId;
    }

    public SecurityScan getSecurityScan() {
        return securityScan;
    }

    public void setSecurityScan(SecurityScan securityScan) {
        this.securityScan = securityScan;
    }

    public String getFlagType() {
        return flagType;
    }

    public void setFlagType(String flagType) {
        this.flagType = flagType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getCweId() {
        return cweId;
    }

    public void setCweId(String cweId) {
        this.cweId = cweId;
    }

    public String getOwaspCategory() {
        return owaspCategory;
    }

    public void setOwaspCategory(String owaspCategory) {
        this.owaspCategory = owaspCategory;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Boolean getIsFalsePositive() {
        return isFalsePositive;
    }

    public void setIsFalsePositive(Boolean isFalsePositive) {
        this.isFalsePositive = isFalsePositive;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getDetectionMetadata() {
        return detectionMetadata;
    }

    public void setDetectionMetadata(String detectionMetadata) {
        this.detectionMetadata = detectionMetadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Boolean getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(Boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    public String getFalsePositiveReason() {
        return falsePositiveReason;
    }

    public void setFalsePositiveReason(String falsePositiveReason) {
        this.falsePositiveReason = falsePositiveReason;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
    }

    // Business logic helpers
    public void markAsResolved(String notes) {
        this.isResolved = true;
        this.resolvedAt = Instant.now();
        this.resolutionNotes = notes;
    }

    public void markAsFalsePositive(String notes) {
        this.isFalsePositive = true;
        this.isResolved = true;
        this.resolvedAt = Instant.now();
        this.resolutionNotes = notes;
    }

    public boolean isCritical() {
        return "critical".equalsIgnoreCase(severity);
    }

    public boolean isHighSeverity() {
        return "high".equalsIgnoreCase(severity) || "critical".equalsIgnoreCase(severity);
    }

    public boolean requiresImmediateAction() {
        return isCritical() && !Boolean.TRUE.equals(isResolved) && !Boolean.TRUE.equals(isFalsePositive);
    }

    public boolean isLowConfidence() {
        return confidenceScore != null && confidenceScore < 70;
    }

    public String getLocation() {
        StringBuilder location = new StringBuilder();
        
        if (filePath != null) {
            location.append(filePath);
        }
        if (className != null) {
            if (location.length() > 0) location.append(" - ");
            location.append(className);
        }
        if (methodName != null) {
            if (location.length() > 0) location.append(".");
            location.append(methodName).append("()");
        }
        if (lineNumber != null) {
            if (location.length() > 0) location.append(":");
            location.append(lineNumber);
        }
        
        return location.toString();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityFlag)) return false;
        SecurityFlag that = (SecurityFlag) o;
        return flagId != null && flagId.equals(that.getFlagId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SecurityFlag{" +
                "flagId=" + flagId +
                ", flagType='" + flagType + '\'' +
                ", severity='" + severity + '\'' +
                ", title='" + title + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", lineNumber=" + lineNumber +
                ", isResolved=" + isResolved +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
