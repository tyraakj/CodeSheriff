package com.backend.CodeSheriff.Security;

import com.backend.CodeSheriff.Entity.*;
import com.backend.CodeSheriff.Model.BobAnalysis;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Layer 3 of Security Pipeline: Hallucination Shield
 * 
 * Validates AI-generated analysis outputs against the actual code structure (AST).
 * Detects when the AI "hallucinates" - makes claims about code that don't match reality.
 * 
 * Validation Checks:
 * - Method existence and signature
 * - Parameter count and types
 * - Return type accuracy
 * - Exception declarations
 * - Annotation presence
 * - Code structure claims
 * - Confidence score vs. actual complexity
 */
@Component
public class HallucinationShield {

    private static final Logger logger = LoggerFactory.getLogger(HallucinationShield.class);

    /**
     * Baseline data captured before Bob analysis.
     */
    public static class ASTBaseline {
        private final String methodName;
        private final String returnType;
        private final List<String> parameters;
        private final List<String> exceptions;
        private final List<String> annotations;
        private final int lineCount;
        private final int cyclomaticComplexity;
        private final boolean hasJavadoc;
        private final String sourceCode;

        public ASTBaseline(MethodDeclaration methodDecl, String sourceCode) {
            this.methodName = methodDecl.getNameAsString();
            this.returnType = methodDecl.getType().asString();
            this.parameters = methodDecl.getParameters().stream()
                .map(p -> p.getType().asString())
                .collect(Collectors.toList());
            this.exceptions = methodDecl.getThrownExceptions().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
            this.annotations = methodDecl.getAnnotations().stream()
                .map(a -> a.getNameAsString())
                .collect(Collectors.toList());
            this.lineCount = methodDecl.getRange()
                .map(r -> r.end.line - r.begin.line + 1)
                .orElse(0);
            this.cyclomaticComplexity = calculateComplexity(methodDecl);
            this.hasJavadoc = methodDecl.getJavadoc().isPresent();
            this.sourceCode = sourceCode;
        }

        private int calculateComplexity(MethodDeclaration method) {
            // Simple complexity calculation based on decision points
            int complexity = 1; // Base complexity
            
            // Count if statements
            complexity += method.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
            
            // Count loops
            complexity += method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
            complexity += method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
            complexity += method.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
            complexity += method.findAll(com.github.javaparser.ast.stmt.ForEachStmt.class).size();
            
            // Count switch cases
            method.findAll(com.github.javaparser.ast.stmt.SwitchStmt.class).forEach(switchStmt -> {
                complexity += switchStmt.getEntries().size();
            });
            
            // Count catch blocks
            complexity += method.findAll(com.github.javaparser.ast.stmt.CatchClause.class).size();
            
            // Count ternary operators
            complexity += method.findAll(com.github.javaparser.ast.expr.ConditionalExpr.class).size();
            
            // Count logical operators (&&, ||)
            complexity += method.findAll(com.github.javaparser.ast.expr.BinaryExpr.class).stream()
                .filter(expr -> expr.getOperator() == com.github.javaparser.ast.expr.BinaryExpr.Operator.AND ||
                               expr.getOperator() == com.github.javaparser.ast.expr.BinaryExpr.Operator.OR)
                .count();
            
            return complexity;
        }

        // Getters
        public String getMethodName() { return methodName; }
        public String getReturnType() { return returnType; }
        public List<String> getParameters() { return parameters; }
        public List<String> getExceptions() { return exceptions; }
        public List<String> getAnnotations() { return annotations; }
        public int getLineCount() { return lineCount; }
        public int getCyclomaticComplexity() { return cyclomaticComplexity; }
        public boolean hasJavadoc() { return hasJavadoc; }
        public String getSourceCode() { return sourceCode; }
    }

    /**
     * Capture AST baseline before Bob analysis.
     */
    public ASTBaseline captureBaseline(MethodDeclaration methodDecl, String sourceCode) {
        logger.debug("Capturing AST baseline for method: {}", methodDecl.getNameAsString());
        return new ASTBaseline(methodDecl, sourceCode);
    }

    /**
     * Validate Bob's analysis output against AST baseline.
     */
    public List<SecurityFlag> validateBobOutput(BobOutput bobOutput, ASTBaseline baseline,
                                                 Method method, JavaClass javaClass,
                                                 SecurityScan securityScan) {
        logger.debug("Validating Bob output for method: {}", method.getMethodName());
        
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Validate method signature claims
        flags.addAll(validateMethodSignature(bobOutput, baseline, method, javaClass, securityScan));
        
        // Validate complexity assessment
        flags.addAll(validateComplexity(bobOutput, baseline, method, javaClass, securityScan));
        
        // Validate test coverage claims
        flags.addAll(validateTestClaims(bobOutput, baseline, method, javaClass, securityScan));
        
        // Validate confidence score
        flags.addAll(validateConfidence(bobOutput, baseline, method, javaClass, securityScan));
        
        if (!flags.isEmpty()) {
            logger.warn("Found {} hallucination issues in Bob output for method {}", 
                       flags.size(), method.getMethodName());
        }
        
        return flags;
    }

    /**
     * Validate method signature claims.
     */
    private List<SecurityFlag> validateMethodSignature(BobOutput bobOutput, ASTBaseline baseline,
                                                         Method method, JavaClass javaClass,
                                                         SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Check if Bob mentions parameters that don't exist
        if (bobOutput.getPurpose() != null || bobOutput.getSummary() != null) {
            String combinedText = (bobOutput.getPurpose() + " " + bobOutput.getSummary()).toLowerCase();
            
            // Look for parameter mentions
            if (combinedText.contains("parameter") || combinedText.contains("argument")) {
                int actualParamCount = baseline.getParameters().size();
                
                // If Bob mentions parameters but method has none
                if (actualParamCount == 0 && 
                    (combinedText.contains("takes") || combinedText.contains("accepts"))) {
                    SecurityFlag flag = createHallucinationFlag(
                        securityScan,
                        "HALLUCINATION_PARAMETERS",
                        "Bob claims method has parameters, but it has none",
                        "Bob's analysis mentions parameters, but the method signature shows no parameters.",
                        method,
                        javaClass,
                        "medium"
                    );
                    flags.add(flag);
                }
            }
        }
        
        return flags;
    }

    /**
     * Validate complexity assessment.
     */
    private List<SecurityFlag> validateComplexity(BobOutput bobOutput, ASTBaseline baseline,
                                                    Method method, JavaClass javaClass,
                                                    SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        if (bobOutput.getComplexityAssessment() != null) {
            String bobComplexity = bobOutput.getComplexityAssessment().toLowerCase();
            int actualComplexity = baseline.getCyclomaticComplexity();
            
            // Define complexity thresholds
            boolean isActuallySimple = actualComplexity <= 5;
            boolean isActuallyComplex = actualComplexity > 15;
            
            // Check for mismatches
            if (bobComplexity.contains("simple") && isActuallyComplex) {
                SecurityFlag flag = createHallucinationFlag(
                    securityScan,
                    "HALLUCINATION_COMPLEXITY_LOW",
                    "Bob underestimates method complexity",
                    String.format("Bob claims method is simple, but cyclomatic complexity is %d (high).", 
                                 actualComplexity),
                    method,
                    javaClass,
                    "high"
                );
                flags.add(flag);
            } else if ((bobComplexity.contains("complex") || bobComplexity.contains("very_complex")) 
                       && isActuallySimple) {
                SecurityFlag flag = createHallucinationFlag(
                    securityScan,
                    "HALLUCINATION_COMPLEXITY_HIGH",
                    "Bob overestimates method complexity",
                    String.format("Bob claims method is complex, but cyclomatic complexity is %d (low).", 
                                 actualComplexity),
                    method,
                    javaClass,
                    "low"
                );
                flags.add(flag);
            }
        }
        
        return flags;
    }

    /**
     * Validate test coverage claims.
     */
    private List<SecurityFlag> validateTestClaims(BobOutput bobOutput, ASTBaseline baseline,
                                                    Method method, JavaClass javaClass,
                                                    SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Check if Bob claims tests exist
        if (Boolean.TRUE.equals(bobOutput.getHasTests())) {
            // We can't definitively prove tests don't exist without scanning test files,
            // but we can flag if confidence is low
            if (bobOutput.getConfidenceScore() != null && bobOutput.getConfidenceScore() < 70) {
                SecurityFlag flag = createHallucinationFlag(
                    securityScan,
                    "HALLUCINATION_TESTS_UNCERTAIN",
                    "Bob claims tests exist with low confidence",
                    String.format("Bob claims tests exist but confidence score is only %d%%.", 
                                 bobOutput.getConfidenceScore()),
                    method,
                    javaClass,
                    "low"
                );
                flags.add(flag);
            }
        }
        
        return flags;
    }

    /**
     * Validate confidence score reasonableness.
     */
    private List<SecurityFlag> validateConfidence(BobOutput bobOutput, ASTBaseline baseline,
                                                    Method method, JavaClass javaClass,
                                                    SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        if (bobOutput.getConfidenceScore() != null) {
            int confidence = bobOutput.getConfidenceScore();
            
            // Flag unreasonably high confidence for complex methods
            if (confidence > 90 && baseline.getCyclomaticComplexity() > 20) {
                SecurityFlag flag = createHallucinationFlag(
                    securityScan,
                    "HALLUCINATION_OVERCONFIDENT",
                    "Bob shows unreasonably high confidence for complex method",
                    String.format("Confidence is %d%% but method has complexity %d.", 
                                 confidence, baseline.getCyclomaticComplexity()),
                    method,
                    javaClass,
                    "medium"
                );
                flags.add(flag);
            }
            
            // Flag very low confidence (might indicate hallucination)
            if (confidence < 30) {
                SecurityFlag flag = createHallucinationFlag(
                    securityScan,
                    "HALLUCINATION_LOW_CONFIDENCE",
                    "Bob shows very low confidence in analysis",
                    String.format("Confidence is only %d%%, suggesting uncertain or hallucinated analysis.", 
                                 confidence),
                    method,
                    javaClass,
                    "medium"
                );
                flags.add(flag);
            }
        }
        
        return flags;
    }

    /**
     * Validate that Bob's summary matches actual code structure.
     */
    public List<SecurityFlag> validateSummaryAccuracy(BobOutput bobOutput, ASTBaseline baseline,
                                                        Method method, JavaClass javaClass,
                                                        SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        if (bobOutput.getSummary() != null) {
            String summary = bobOutput.getSummary().toLowerCase();
            String sourceCode = baseline.getSourceCode().toLowerCase();
            
            // Check for claims about code that doesn't exist
            List<String> suspiciousClaims = List.of(
                "database", "sql", "query", "connection",
                "network", "http", "api call", "rest",
                "file", "io", "read", "write",
                "thread", "async", "concurrent"
            );
            
            for (String claim : suspiciousClaims) {
                if (summary.contains(claim) && !sourceCode.contains(claim)) {
                    SecurityFlag flag = createHallucinationFlag(
                        securityScan,
                        "HALLUCINATION_FUNCTIONALITY",
                        "Bob claims functionality not present in code",
                        String.format("Bob mentions '%s' but it's not found in the actual code.", claim),
                        method,
                        javaClass,
                        "medium"
                    );
                    flags.add(flag);
                }
            }
        }
        
        return flags;
    }

    /**
     * Create a hallucination security flag.
     */
    private SecurityFlag createHallucinationFlag(SecurityScan securityScan, String flagType,
                                                   String title, String description,
                                                   Method method, JavaClass javaClass,
                                                   String severity) {
        SecurityFlag flag = new SecurityFlag(securityScan, flagType, severity, title);
        
        flag.setDescription(description);
        flag.setFilePath(javaClass.getFilePath());
        flag.setClassName(javaClass.getClassName());
        flag.setMethodName(method.getMethodName());
        
        flag.setRecommendation(
            "Review Bob's analysis carefully. The AI may have hallucinated or misunderstood the code. " +
            "Verify claims against the actual source code before taking action. " +
            "Consider re-running the analysis or adjusting the prompt."
        );
        
        flag.setCweId("CWE-670"); // Always-Incorrect Control Flow Implementation
        flag.setOwaspCategory("A08:2021 – Software and Data Integrity Failures");
        flag.setConfidenceScore(75);
        
        return flag;
    }

    /**
     * Get validation statistics.
     */
    public ValidationStatistics getStatistics(List<SecurityFlag> flags) {
        long complexityIssues = flags.stream()
            .filter(f -> f.getFlagType().contains("COMPLEXITY"))
            .count();
        
        long parameterIssues = flags.stream()
            .filter(f -> f.getFlagType().contains("PARAMETERS"))
            .count();
        
        long confidenceIssues = flags.stream()
            .filter(f -> f.getFlagType().contains("CONFIDENCE"))
            .count();
        
        long functionalityIssues = flags.stream()
            .filter(f -> f.getFlagType().contains("FUNCTIONALITY"))
            .count();
        
        return new ValidationStatistics(complexityIssues, parameterIssues, 
                                        confidenceIssues, functionalityIssues);
    }

    /**
     * Statistics for hallucination detection.
     */
    public static class ValidationStatistics {
        private final long complexityIssues;
        private final long parameterIssues;
        private final long confidenceIssues;
        private final long functionalityIssues;

        public ValidationStatistics(long complexityIssues, long parameterIssues,
                                    long confidenceIssues, long functionalityIssues) {
            this.complexityIssues = complexityIssues;
            this.parameterIssues = parameterIssues;
            this.confidenceIssues = confidenceIssues;
            this.functionalityIssues = functionalityIssues;
        }

        public long getComplexityIssues() { return complexityIssues; }
        public long getParameterIssues() { return parameterIssues; }
        public long getConfidenceIssues() { return confidenceIssues; }
        public long getFunctionalityIssues() { return functionalityIssues; }
        public long getTotalIssues() { 
            return complexityIssues + parameterIssues + confidenceIssues + functionalityIssues; 
        }
    }
}

// Made with Bob
