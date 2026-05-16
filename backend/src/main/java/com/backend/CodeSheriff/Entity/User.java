package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing authenticated users in the system.
 * Maps to the 'users' table in Supabase PostgreSQL.
 * 
 * This entity is synchronized with Supabase Auth via triggers.
 * The user_id is the Supabase Auth UUID.
 */
@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Analysis> analyses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AuditTrail> auditTrails = new ArrayList<>();

    // Constructors
    public User() {
    }

    public User(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    // Getters and Setters
    public UUID getId() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    public List<Analysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<Analysis> analyses) {
        this.analyses = analyses;
    }

    public List<AuditTrail> getAuditTrails() {
        return auditTrails;
    }

    public void setAuditTrails(List<AuditTrail> auditTrails) {
        this.auditTrails = auditTrails;
    }

    // Helper methods for bidirectional relationships
    public void addAnalysis(Analysis analysis) {
        analyses.add(analysis);
        analysis.setUser(this);
    }

    public void removeAnalysis(Analysis analysis) {
        analyses.remove(analysis);
        analysis.setUser(null);
    }

    public void addAuditTrail(AuditTrail auditTrail) {
        auditTrails.add(auditTrail);
        auditTrail.setUser(this);
    }

    public void removeAuditTrail(AuditTrail auditTrail) {
        auditTrails.remove(auditTrail);
        auditTrail.setUser(null);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userId != null && userId.equals(user.getUserId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
