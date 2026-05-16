package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * AuditTrail entity representing an append-only audit log entry.
 * Maps to the 'audit_trail' table in Supabase PostgreSQL.
 * 
 * This entity is IMMUTABLE - once created, records cannot be modified or deleted.
 * All system actions are logged here for compliance and security monitoring.
 * 
 * Note: Deletion operations require service role bypass of RLS policies.
 */
@Entity
@Immutable
@Table(name = "audit_trail", schema = "public", indexes = {
    @Index(name = "idx_audit_trail_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_trail_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_trail_resource_type", columnList = "resource_type"),
    @Index(name = "idx_audit_trail_created_at", columnList = "created_at")
})
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", nullable = false, updatable = false)
    private UUID auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_audit_trail_user"))
    private User user;

    @NotBlank(message = "Action type is required")
    @Size(max = 100, message = "Action type must not exceed 100 characters")
    @Column(name = "action_type", nullable = false, length = 100, updatable = false)
    private String actionType; // upload, analyze, security_scan, login, logout, etc.

    @Size(max = 100, message = "Resource type must not exceed 100 characters")
    @Column(name = "resource_type", length = 100, updatable = false)
    private String resourceType; // analysis, method, security_scan, etc.

    @Column(name = "resource_id", updatable = false)
    private UUID resourceId;

    @Column(name = "action_details", columnDefinition = "TEXT", updatable = false)
    private String actionDetails; // JSON string with additional details

    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Column(name = "status", length = 50, updatable = false)
    private String status; // success, failure, pending

    @Column(name = "error_message", columnDefinition = "TEXT", updatable = false)
    private String errorMessage;

    @Size(max = 100, message = "IP address must not exceed 100 characters")
    @Column(name = "ip_address", length = 100, updatable = false)
    private String ipAddress;

    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Column(name = "user_agent", length = 500, updatable = false)
    private String userAgent;

    @Column(name = "request_id", updatable = false)
    private UUID requestId; // Correlation ID for tracking related actions

    @Column(name = "duration_ms", updatable = false)
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public AuditTrail() {
    }

    public AuditTrail(String actionType) {
        this.actionType = actionType;
        this.status = "pending";
    }

    public AuditTrail(User user, String actionType, String resourceType, UUID resourceId) {
        this.user = user;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.status = "pending";
    }

    // Getters only (no setters for immutable fields after construction)
    public UUID getAuditId() {
        return auditId;
    }

    public User getUser() {
        return user;
    }

    public String getActionType() {
        return actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Limited setters for fields that can be set before persistence
    public void setUser(User user) {
        this.user = user;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    // Business logic helpers
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public boolean isFailure() {
        return "failure".equalsIgnoreCase(status);
    }

    public boolean isSecurityRelated() {
        return actionType != null && (
            actionType.contains("security") ||
            actionType.contains("login") ||
            actionType.contains("logout") ||
            actionType.contains("auth")
        );
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (user != null && user.getEmail() != null) {
            summary.append(user.getEmail()).append(" ");
        } else {
            summary.append("System ");
        }
        
        summary.append(actionType);
        
        if (resourceType != null) {
            summary.append(" ").append(resourceType);
        }
        
        if (resourceId != null) {
            summary.append(" (").append(resourceId).append(")");
        }
        
        summary.append(" - ").append(status);
        
        return summary.toString();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditTrail)) return false;
        AuditTrail that = (AuditTrail) o;
        return auditId != null && auditId.equals(that.getAuditId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AuditTrail{" +
                "auditId=" + auditId +
                ", actionType='" + actionType + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", status='" + status + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    // Builder pattern for easier construction
    public static class Builder {
        private final AuditTrail auditTrail;

        public Builder(String actionType) {
            this.auditTrail = new AuditTrail(actionType);
        }

        public Builder user(User user) {
            auditTrail.setUser(user);
            return this;
        }

        public Builder resourceType(String resourceType) {
            auditTrail.setResourceType(resourceType);
            return this;
        }

        public Builder resourceId(UUID resourceId) {
            auditTrail.setResourceId(resourceId);
            return this;
        }

        public Builder actionDetails(String actionDetails) {
            auditTrail.setActionDetails(actionDetails);
            return this;
        }

        public Builder status(String status) {
            auditTrail.setStatus(status);
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            auditTrail.setErrorMessage(errorMessage);
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            auditTrail.setIpAddress(ipAddress);
            return this;
        }

        public Builder userAgent(String userAgent) {
            auditTrail.setUserAgent(userAgent);
            return this;
        }

        public Builder requestId(UUID requestId) {
            auditTrail.setRequestId(requestId);
            return this;
        }

        public Builder durationMs(Long durationMs) {
            auditTrail.setDurationMs(durationMs);
            return this;
        }

        public AuditTrail build() {
            return auditTrail;
        }
    }
}

// Made with Bob
