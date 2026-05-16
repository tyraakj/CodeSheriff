package com.backend.CodeSheriff.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BobAnalysis {
    private String analysis;
    private String complexityAssessment;
    private String testCoverage;
    private double confidenceScore;
    private String whatItDoes;
    private String intentVsReality;
    private String whereToStart;
    private boolean hasTests;
    private int lineCount;
    private String modelUsed;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private long responseTimeMs;
}
