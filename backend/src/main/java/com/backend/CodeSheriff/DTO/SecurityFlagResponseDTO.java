package com.backend.CodeSheriff.DTO;

import com.backend.CodeSheriff.Entity.SecurityFlag;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SecurityFlag entity responses
 * Provides detailed information about individual security vulnerabilities
 * 
 * @author CodeSheriff Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecurityFlagResponseDTO {
    
    private Long id;
    private Long securityScanId;
    private String flagType;
    private String severity;
    private String title;
    private String description;
    private String location;
    private Integer lineNumber;
    private String codeSnippet;
    private String recommendation;
    private String cweId;
    private String owaspCategory;
    private Double confidenceScore;
    private String status;
    private String resolution;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private Boolean falsePositive;
    private String falsePositiveReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Context information
    private String className;
    private String methodName;
    private String packageName;
    
    /**
     * Factory method to create DTO from SecurityFlag entity
     */
    public static SecurityFlagResponseDTO fromEntity(SecurityFlag flag) {
        if (flag == null) {
            return null;
        }
        
        SecurityFlagResponseDTOBuilder builder = SecurityFlagResponseDTO.builder()
            .id(flag.getId())
            .securityScanId(flag.getSecurityScan() != null ? flag.getSecurityScan().getId() : null)
            .flagType(flag.getFlagType())
            .severity(flag.getSeverity())
            .title(flag.getTitle())
            .description(flag.getDescription())
            .location(flag.getLocation())
            .lineNumber(flag.getLineNumber())
            .codeSnippet(flag.getCodeSnippet())
            .recommendation(flag.getRecommendation())
            .cweId(flag.getCweId())
            .owaspCategory(flag.getOwaspCategory())
            .confidenceScore(flag.getConfidenceScore())
            .status(flag.getStatus())
            .resolution(flag.getResolution())
            .resolvedBy(flag.getResolvedBy())
            .resolvedAt(flag.getResolvedAt())
            .falsePositive(flag.getFalsePositive())
            .falsePositiveReason(flag.getFalsePositiveReason())
            .createdAt(flag.getCreatedAt())
            .updatedAt(flag.getUpdatedAt());
        
        // Add context from related entities
        if (flag.getMethod() != null) {
            builder.methodName(flag.getMethod().getMethodName());
            if (flag.getMethod().getJavaClass() != null) {
                builder.className(flag.getMethod().getJavaClass().getClassName());
                builder.packageName(flag.getMethod().getJavaClass().getPackageName());
            }
        } else if (flag.getJavaClass() != null) {
            builder.className(flag.getJavaClass().getClassName());
            builder.packageName(flag.getJavaClass().getPackageName());
        }
        
        return builder.build();
    }
    
    /**
     * Check if this flag is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(severity);
    }
    
    /**
     * Check if this flag is high severity
     */
    public boolean isHighSeverity() {
        return "HIGH".equalsIgnoreCase(severity);
    }
    
    /**
     * Check if this flag is resolved
     */
    public boolean isResolved() {
        return "RESOLVED".equalsIgnoreCase(status);
    }
    
    /**
     * Check if this flag is a false positive
     */
    public boolean isFalsePositive() {
        return Boolean.TRUE.equals(falsePositive);
    }
    
    /**
     * Get severity level as integer (for sorting)
     * CRITICAL=4, HIGH=3, MEDIUM=2, LOW=1, INFO=0
     */
    public int getSeverityLevel() {
        if (severity == null) return 0;
        switch (severity.toUpperCase()) {
            case "CRITICAL": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }
    
    /**
     * Get full location string (package.class.method:line)
     */
    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append(packageName).append(".");
        }
        if (className != null) {
            sb.append(className);
        }
        if (methodName != null) {
            sb.append(".").append(methodName);
        }
        if (lineNumber != null) {
            sb.append(":").append(lineNumber);
        }
        return sb.toString();
    }
}

// Made with Bob
