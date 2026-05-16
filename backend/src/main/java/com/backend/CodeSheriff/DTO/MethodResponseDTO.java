package com.backend.CodeSheriff.DTO;

import com.backend.CodeSheriff.Entity.Method;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for Method entity responses
 * Provides detailed information about parsed Java methods
 * 
 * @author CodeSheriff Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodResponseDTO {
    
    private UUID id;
    private UUID javaClassId;
    private String className;
    private String packageName;
    private String methodName;
    private String returnType;
    private List<String> parameters;
    private List<String> modifiers;
    private String visibility;
    private Boolean isStatic;
    private Boolean isFinal;
    private Boolean isAbstract;
    private Boolean isSynchronized;
    private Boolean isConstructor;
    private Integer lineStart;
    private Integer lineEnd;
    private Integer linesOfCode;
    private Integer cyclomaticComplexity;
    private Integer cognitiveComplexity;
    private Integer parameterCount;
    private Integer localVariableCount;
    private String sourceCode;
    private String javadoc;
    private List<String> annotations;
    private List<String> thrownExceptions;
    private List<String> calledMethods;
    private Boolean hasLoops;
    private Boolean hasConditionals;
    private Boolean hasTryCatch;
    private String complexityLevel;
    private Instant createdAt;
    private java.time.LocalDateTime updatedAt;
    
    // Analysis results
    private BobOutputResponseDTO bobAnalysis;
    private List<SecurityFlagResponseDTO> securityFlags;
    private Integer securityFlagCount;
    private Integer criticalFlagCount;
    
    /**
     * Factory method to create DTO from Method entity
     */
    public static MethodResponseDTO fromEntity(Method method) {
        return fromEntity(method, false, false);
    }
    
    /**
     * Factory method with options to include related data
     */
    public static MethodResponseDTO fromEntity(Method method, boolean includeBobAnalysis, boolean includeSecurityFlags) {
        if (method == null) {
            return null;
        }
        
        MethodResponseDTOBuilder builder = MethodResponseDTO.builder()
            .id(method.getId())
            .javaClassId(method.getJavaClass() != null ? method.getJavaClass().getClassId() : null)
            .methodName(method.getMethodName())
            .returnType(method.getReturnType())
            .parameters(method.getParameters())
            .modifiers(method.getModifiers())
            .visibility(method.getVisibility())
            .isStatic(method.getIsStatic())
            .isFinal(method.getIsFinal())
            .isAbstract(method.getIsAbstract())
            .isSynchronized(method.getIsSynchronized())
            .isConstructor(method.getIsConstructor())
            .lineStart(method.getLineStart())
            .lineEnd(method.getLineEnd())
            .linesOfCode(method.getLinesOfCode())
            .cyclomaticComplexity(method.getCyclomaticComplexity())
            .cognitiveComplexity(method.getCognitiveComplexity())
            .parameterCount(method.getParameterCount())
            .localVariableCount(method.getLocalVariableCount())
            .sourceCode(method.getSourceCode())
            .javadoc(method.getJavadoc())
            .annotations(method.getAnnotations())
            .thrownExceptions(method.getThrowsExceptions())
            .calledMethods(splitCsv(method.getCalledMethods()))
            .hasLoops(method.getHasLoops())
            .hasConditionals(method.getHasConditionals())
            .hasTryCatch(method.getHasTryCatch())
            .complexityLevel(method.getComplexityLevel())
            .createdAt(method.getCreatedAt())
            .updatedAt(method.getUpdatedAt());
        
        // Add class context
        if (method.getJavaClass() != null) {
            builder.className(method.getJavaClass().getClassName());
            builder.packageName(method.getJavaClass().getPackageName());
        }
        
        // Include Bob analysis if requested
        if (includeBobAnalysis && method.getBobOutputs() != null && !method.getBobOutputs().isEmpty()) {
            // Get the most recent Bob analysis
            builder.bobAnalysis(BobOutputResponseDTO.fromEntity(
                method.getBobOutputs().stream()
                    .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .orElse(null)
            ));
        }
        
        // Include security flags if requested
        if (includeSecurityFlags && method.getSecurityFlags() != null) {
            List<SecurityFlagResponseDTO> flags = method.getSecurityFlags().stream()
                .map(SecurityFlagResponseDTO::fromEntity)
                .collect(Collectors.toList());
            builder.securityFlags(flags);
            builder.securityFlagCount(flags.size());
            builder.criticalFlagCount((int) flags.stream()
                .filter(SecurityFlagResponseDTO::isCritical)
                .count());
        }
        
        return builder.build();
    }
    
    /**
     * Get method signature
     */
    public String getMethodSignature() {
        StringBuilder sb = new StringBuilder();
        
        if (visibility != null) {
            sb.append(visibility).append(" ");
        }
        if (Boolean.TRUE.equals(isStatic)) {
            sb.append("static ");
        }
        if (Boolean.TRUE.equals(isFinal)) {
            sb.append("final ");
        }
        if (returnType != null) {
            sb.append(returnType).append(" ");
        }
        sb.append(methodName);
        sb.append("(");
        if (parameters != null && !parameters.isEmpty()) {
            sb.append(String.join(", ", parameters));
        }
        sb.append(")");
        
        return sb.toString();
    }
    
    /**
     * Get full qualified method name
     */
    public String getFullyQualifiedName() {
        StringBuilder sb = new StringBuilder();
        if (packageName != null) {
            sb.append(packageName).append(".");
        }
        if (className != null) {
            sb.append(className).append(".");
        }
        sb.append(methodName);
        return sb.toString();
    }
    
    /**
     * Check if method is complex
     */
    public boolean isComplex() {
        return cyclomaticComplexity != null && cyclomaticComplexity > 10;
    }
    
    /**
     * Check if method is very complex
     */
    public boolean isVeryComplex() {
        return cyclomaticComplexity != null && cyclomaticComplexity > 20;
    }
    
    /**
     * Check if method has security issues
     */
    public boolean hasSecurityIssues() {
        return securityFlagCount != null && securityFlagCount > 0;
    }
    
    /**
     * Check if method has critical security issues
     */
    public boolean hasCriticalSecurityIssues() {
        return criticalFlagCount != null && criticalFlagCount > 0;
    }
    
    /**
     * Get complexity category
     */
    public String getComplexityCategory() {
        if (cyclomaticComplexity == null) return "UNKNOWN";
        if (cyclomaticComplexity <= 5) return "SIMPLE";
        if (cyclomaticComplexity <= 10) return "MODERATE";
        if (cyclomaticComplexity <= 20) return "COMPLEX";
        return "VERY_COMPLEX";
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toList());
    }
}

// Made with Bob
