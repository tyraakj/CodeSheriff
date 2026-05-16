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
 * Analysis entity representing a code analysis session.
 * Maps to the 'analyses' table in Supabase PostgreSQL.
 * 
 * Each analysis represents one ZIP file upload and contains
 * multiple Java classes that were analyzed.
 */
@Entity
@Table(name = "analyses", schema = "public", indexes = {
    @Index(name = "idx_analyses_user_id", columnList = "user_id"),
    @Index(name = "idx_analyses_status", columnList = "status"),
    @Index(name = "idx_analyses_created_at", columnList = "created_at")
})
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analysis_id", nullable = false, updatable = false)
    private UUID analysisId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_analyses_user"))
    private User user;

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Size(max = 255, message = "ZIP filename must not exceed 255 characters")
    @Column(name = "zip_filename", length = 255)
    private String zipFilename;

    @Column(name = "total_classes")
    private Integer totalClasses = 0;

    @Column(name = "total_methods")
    private Integer totalMethods = 0;

    @Column(name = "analyzed_methods")
    private Integer analyzedMethods = 0;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Column(name = "status", nullable = false, length = 50)
    private String status = "pending"; // pending, in_progress, completed, failed

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JavaClass> javaClasses = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SecurityScan> securityScans = new ArrayList<>();

    // Constructors
    public Analysis() {
    }

    public Analysis(User user, String projectName) {
        this.user = user;
        this.projectName = projectName;
    }

    // Getters and Setters
    public UUID getId() {
        return analysisId;
    }

    public UUID getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(UUID analysisId) {
        this.analysisId = analysisId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getZipFilename() {
        return zipFilename;
    }

    public void setZipFilename(String zipFilename) {
        this.zipFilename = zipFilename;
    }

    public Integer getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(Integer totalClasses) {
        this.totalClasses = totalClasses;
    }

    public Integer getTotalMethods() {
        return totalMethods;
    }

    public void setTotalMethods(Integer totalMethods) {
        this.totalMethods = totalMethods;
    }

    public Integer getAnalyzedMethods() {
        return analyzedMethods;
    }

    public void setAnalyzedMethods(Integer analyzedMethods) {
        this.analyzedMethods = analyzedMethods;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<JavaClass> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(List<JavaClass> javaClasses) {
        this.javaClasses = javaClasses;
    }

    public List<SecurityScan> getSecurityScans() {
        return securityScans;
    }

    public void setSecurityScans(List<SecurityScan> securityScans) {
        this.securityScans = securityScans;
    }

    // Helper methods for bidirectional relationships
    public void addJavaClass(JavaClass javaClass) {
        javaClasses.add(javaClass);
        javaClass.setAnalysis(this);
    }

    public void removeJavaClass(JavaClass javaClass) {
        javaClasses.remove(javaClass);
        javaClass.setAnalysis(null);
    }

    public void addSecurityScan(SecurityScan securityScan) {
        securityScans.add(securityScan);
        securityScan.setAnalysis(this);
    }

    public void removeSecurityScan(SecurityScan securityScan) {
        securityScans.remove(securityScan);
        securityScan.setAnalysis(null);
    }

    // Business logic helpers
    public void markAsStarted() {
        this.status = "in_progress";
        this.startedAt = Instant.now();
    }

    public void markAsCompleted() {
        this.status = "completed";
        this.completedAt = Instant.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = "failed";
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public void incrementAnalyzedMethods() {
        this.analyzedMethods = (this.analyzedMethods == null ? 0 : this.analyzedMethods) + 1;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Analysis)) return false;
        Analysis analysis = (Analysis) o;
        return analysisId != null && analysisId.equals(analysis.getAnalysisId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "analysisId=" + analysisId +
                ", projectName='" + projectName + '\'' +
                ", status='" + status + '\'' +
                ", totalClasses=" + totalClasses +
                ", totalMethods=" + totalMethods +
                ", analyzedMethods=" + analyzedMethods +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
