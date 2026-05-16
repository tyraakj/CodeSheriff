package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * BobOutput entity representing AI analysis results from IBM watsonx.ai (Bob).
 * Maps to the 'bob_outputs' table in Supabase PostgreSQL.
 * 
 * Each BobOutput is linked to exactly one Method and contains the AI's
 * assessment of code quality, security, and recommendations.
 */
@Entity
@Table(name = "bob_outputs", schema = "public", indexes = {
    @Index(name = "idx_bob_outputs_method_id", columnList = "method_id"),
    @Index(name = "idx_bob_outputs_risk_level", columnList = "risk_level"),
    @Index(name = "idx_bob_outputs_confidence_score", columnList = "confidence_score")
})
public class BobOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "bob_output_id", nullable = false, updatable = false)
    private UUID bobOutputId;

    @NotNull(message = "Method is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "method_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_bob_outputs_method"))
    private Method method;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "risk_level", length = 50)
    private String riskLevel; // low, medium, high, critical

    @Column(name = "risk_explanation", columnDefinition = "TEXT")
    private String riskExplanation;

    @Column(name = "security_concerns", columnDefinition = "TEXT")
    private String securityConcerns;

    @Column(name = "code_smells", columnDefinition = "TEXT")
    private String codeSmells;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "best_practices", columnDefinition = "TEXT")
    private String bestPractices;

    @Min(value = 0, message = "Confidence score must be between 0 and 100")
    @Max(value = 100, message = "Confidence score must be between 0 and 100")
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "has_tests")
    private Boolean hasTests;

    @Column(name = "test_coverage_estimate")
    private Integer testCoverageEstimate;

    @Column(name = "complexity_assessment", length = 50)
    private String complexityAssessment; // simple, moderate, complex, very_complex

    @Column(name = "maintainability_score")
    private Integer maintainabilityScore;

    @Column(name = "performance_notes", columnDefinition = "TEXT")
    private String performanceNotes;

    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "model_used", length = 100)
    private String modelUsed; // e.g., "meta-llama/llama-3-70b-instruct"

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Additional fields for DTOs and controllers
    @Column(name = "analysis_text", columnDefinition = "TEXT")
    private String analysisText;

    @Column(name = "test_coverage")
    private String testCoverage;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructors
    public BobOutput() {
    }

    public BobOutput(Method method) {
        this.method = method;
    }

    public BobOutput(Method method, User user) {
        this.method = method;
        // User is not stored in BobOutput, but constructor exists for compatibility
    }

    // Getters and Setters
    public UUID getBobOutputId() {
        return bobOutputId;
    }

    public void setBobOutputId(UUID bobOutputId) {
        this.bobOutputId = bobOutputId;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(String riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public String getSecurityConcerns() {
        return securityConcerns;
    }

    public void setSecurityConcerns(String securityConcerns) {
        this.securityConcerns = securityConcerns;
    }

    public String getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(String codeSmells) {
        this.codeSmells = codeSmells;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getBestPractices() {
        return bestPractices;
    }

    public void setBestPractices(String bestPractices) {
        this.bestPractices = bestPractices;
    }

    public Integer getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Integer confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Boolean getHasTests() {
        return hasTests;
    }

    public void setHasTests(Boolean hasTests) {
        this.hasTests = hasTests;
    }

    public Integer getTestCoverageEstimate() {
        return testCoverageEstimate;
    }

    public void setTestCoverageEstimate(Integer testCoverageEstimate) {
        this.testCoverageEstimate = testCoverageEstimate;
    }

    public String getComplexityAssessment() {
        return complexityAssessment;
    }

    public void setComplexityAssessment(String complexityAssessment) {
        this.complexityAssessment = complexityAssessment;
    }

    public Integer getMaintainabilityScore() {
        return maintainabilityScore;
    }

    public void setMaintainabilityScore(Integer maintainabilityScore) {
        this.maintainabilityScore = maintainabilityScore;
    }

    public String getPerformanceNotes() {
        return performanceNotes;
    }

    public void setPerformanceNotes(String performanceNotes) {
        this.performanceNotes = performanceNotes;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getAnalysisText() {
        return analysisText;
    }

    public void setAnalysisText(String analysisText) {
        this.analysisText = analysisText;
    }

    public String getTestCoverage() {
        return testCoverage;
    }

    public void setTestCoverage(String testCoverage) {
        this.testCoverage = testCoverage;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    // Business logic helpers
    public boolean isHighRisk() {
        return "high".equalsIgnoreCase(riskLevel) || "critical".equalsIgnoreCase(riskLevel);
    }

    public boolean isLowConfidence() {
        return confidenceScore != null && confidenceScore < 70;
    }

    public boolean hasSecurityIssues() {
        return securityConcerns != null && !securityConcerns.trim().isEmpty();
    }

    public boolean isComplexMethod() {
        return "complex".equalsIgnoreCase(complexityAssessment) || 
               "very_complex".equalsIgnoreCase(complexityAssessment);
    }

    public boolean needsImprovement() {
        return isHighRisk() || 
               (maintainabilityScore != null && maintainabilityScore < 60) ||
               hasSecurityIssues();
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BobOutput)) return false;
        BobOutput bobOutput = (BobOutput) o;
        return bobOutputId != null && bobOutputId.equals(bobOutput.getBobOutputId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "BobOutput{" +
                "bobOutputId=" + bobOutputId +
                ", riskLevel='" + riskLevel + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", complexityAssessment='" + complexityAssessment + '\'' +
                ", maintainabilityScore=" + maintainabilityScore +
                ", hasTests=" + hasTests +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
