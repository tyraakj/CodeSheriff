package com.backend.CodeSheriff.Security;

import com.backend.CodeSheriff.Entity.JavaClass;
import com.backend.CodeSheriff.Entity.Method;
import com.backend.CodeSheriff.Entity.SecurityFlag;
import com.backend.CodeSheriff.Entity.SecurityScan;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Layer 1 of Security Pipeline: ASI01 Injection Detection
 * 
 * Detects potential prompt injection attacks in code comments and string literals.
 * These attacks attempt to manipulate AI analysis by embedding malicious instructions
 * in comments or strings that the AI might interpret as commands.
 * 
 * Detection Patterns:
 * - Ignore/disregard instructions
 * - System prompts or role changes
 * - Instruction overrides
 * - Jailbreak attempts
 * - Output manipulation commands
 */
@Component
public class ASI01InjectionDetector {

    private static final Logger logger = LoggerFactory.getLogger(ASI01InjectionDetector.class);

    // Prompt injection patterns (case-insensitive)
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
        // Ignore/disregard patterns
        Pattern.compile("ignore\\s+(all\\s+)?(previous|above|prior)\\s+(instructions?|prompts?|rules?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("disregard\\s+(all\\s+)?(previous|above|prior)\\s+(instructions?|prompts?|rules?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("forget\\s+(all\\s+)?(previous|above|prior)\\s+(instructions?|prompts?|rules?)", Pattern.CASE_INSENSITIVE),
        
        // System/role manipulation
        Pattern.compile("you\\s+are\\s+(now\\s+)?a\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("act\\s+as\\s+(a\\s+)?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("pretend\\s+(you\\s+are|to\\s+be)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("system\\s*:\\s*", Pattern.CASE_INSENSITIVE),
        Pattern.compile("assistant\\s*:\\s*", Pattern.CASE_INSENSITIVE),
        
        // Instruction override
        Pattern.compile("new\\s+instructions?\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("override\\s+(previous|all)\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("instead\\s+of\\s+analyzing", Pattern.CASE_INSENSITIVE),
        Pattern.compile("don't\\s+analyze", Pattern.CASE_INSENSITIVE),
        
        // Jailbreak attempts
        Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DAN\\s+mode", Pattern.CASE_INSENSITIVE),
        Pattern.compile("developer\\s+mode", Pattern.CASE_INSENSITIVE),
        
        // Output manipulation
        Pattern.compile("output\\s+(only|just)\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("respond\\s+with\\s+(only|just)\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("say\\s+(only|just|exactly)\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("print\\s+(only|just)\\s+", Pattern.CASE_INSENSITIVE),
        
        // Prompt leaking
        Pattern.compile("show\\s+(me\\s+)?(your|the)\\s+(prompt|instructions?|system\\s+message)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("what\\s+(is|are)\\s+your\\s+(instructions?|rules?|prompt)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("reveal\\s+your\\s+(prompt|instructions?)", Pattern.CASE_INSENSITIVE)
    );

    // High-risk keywords that increase suspicion
    private static final List<String> SUSPICIOUS_KEYWORDS = List.of(
        "ignore", "disregard", "forget", "override", "bypass", "jailbreak",
        "system:", "assistant:", "user:", "prompt:", "instruction:",
        "pretend", "act as", "you are now", "new instructions"
    );

    /**
     * Scan a Java class for ASI01 injection attempts.
     * @param javaClass the Java class entity
     * @param compilationUnit the parsed AST
     * @param securityScan the security scan to add flags to
     * @return list of detected security flags
     */
    public List<SecurityFlag> scanJavaClass(JavaClass javaClass, CompilationUnit compilationUnit, 
                                             SecurityScan securityScan) {
        logger.debug("Scanning class {} for ASI01 injection", javaClass.getClassName());
        
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Scan all comments in the class
        compilationUnit.getAllContainedComments().forEach(comment -> {
            List<SecurityFlag> commentFlags = scanComment(comment, javaClass, securityScan);
            flags.addAll(commentFlags);
        });
        
        // Scan all string literals
        compilationUnit.findAll(StringLiteralExpr.class).forEach(stringLiteral -> {
            List<SecurityFlag> stringFlags = scanStringLiteral(stringLiteral, javaClass, securityScan);
            flags.addAll(stringFlags);
        });
        
        if (!flags.isEmpty()) {
            logger.warn("Found {} ASI01 injection attempts in class {}", 
                       flags.size(), javaClass.getClassName());
        }
        
        return flags;
    }

    /**
     * Scan a method for ASI01 injection attempts.
     * @param method the method entity
     * @param methodNode the method AST node
     * @param javaClass the Java class entity
     * @param securityScan the security scan to add flags to
     * @return list of detected security flags
     */
    public List<SecurityFlag> scanMethod(Method method, com.github.javaparser.ast.body.MethodDeclaration methodNode,
                                          JavaClass javaClass, SecurityScan securityScan) {
        logger.debug("Scanning method {} for ASI01 injection", method.getMethodName());
        
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Scan method comments
        methodNode.getAllContainedComments().forEach(comment -> {
            List<SecurityFlag> commentFlags = scanComment(comment, javaClass, method, securityScan);
            flags.addAll(commentFlags);
        });
        
        // Scan method string literals
        methodNode.findAll(StringLiteralExpr.class).forEach(stringLiteral -> {
            List<SecurityFlag> stringFlags = scanStringLiteral(stringLiteral, javaClass, method, securityScan);
            flags.addAll(stringFlags);
        });
        
        return flags;
    }

    /**
     * Scan a comment for injection patterns.
     */
    private List<SecurityFlag> scanComment(Comment comment, JavaClass javaClass, SecurityScan securityScan) {
        return scanComment(comment, javaClass, null, securityScan);
    }

    /**
     * Scan a comment for injection patterns.
     */
    private List<SecurityFlag> scanComment(Comment comment, JavaClass javaClass, Method method, 
                                            SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        String content = comment.getContent();
        
        // Check each injection pattern
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(content).find()) {
                SecurityFlag flag = createInjectionFlag(
                    securityScan,
                    "ASI01_INJECTION_COMMENT",
                    "Potential prompt injection in comment",
                    content,
                    javaClass,
                    method,
                    comment.getRange().map(r -> r.begin.line).orElse(null),
                    pattern.pattern()
                );
                flags.add(flag);
            }
        }
        
        // Check for suspicious keyword density
        if (hasSuspiciousKeywordDensity(content)) {
            SecurityFlag flag = createInjectionFlag(
                securityScan,
                "ASI01_SUSPICIOUS_COMMENT",
                "Comment contains multiple suspicious keywords",
                content,
                javaClass,
                method,
                comment.getRange().map(r -> r.begin.line).orElse(null),
                "High density of suspicious keywords"
            );
            flags.add(flag);
        }
        
        return flags;
    }

    /**
     * Scan a string literal for injection patterns.
     */
    private List<SecurityFlag> scanStringLiteral(StringLiteralExpr stringLiteral, JavaClass javaClass, 
                                                   SecurityScan securityScan) {
        return scanStringLiteral(stringLiteral, javaClass, null, securityScan);
    }

    /**
     * Scan a string literal for injection patterns.
     */
    private List<SecurityFlag> scanStringLiteral(StringLiteralExpr stringLiteral, JavaClass javaClass, 
                                                   Method method, SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        String value = stringLiteral.getValue();
        
        // Only scan strings longer than 20 characters (avoid false positives)
        if (value.length() < 20) {
            return flags;
        }
        
        // Check each injection pattern
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(value).find()) {
                SecurityFlag flag = createInjectionFlag(
                    securityScan,
                    "ASI01_INJECTION_STRING",
                    "Potential prompt injection in string literal",
                    value,
                    javaClass,
                    method,
                    stringLiteral.getRange().map(r -> r.begin.line).orElse(null),
                    pattern.pattern()
                );
                flags.add(flag);
            }
        }
        
        return flags;
    }

    /**
     * Check if content has suspicious keyword density.
     */
    private boolean hasSuspiciousKeywordDensity(String content) {
        String lowerContent = content.toLowerCase();
        long keywordCount = SUSPICIOUS_KEYWORDS.stream()
            .filter(lowerContent::contains)
            .count();
        
        // Flag if 3 or more suspicious keywords in content
        return keywordCount >= 3;
    }

    /**
     * Create a security flag for injection detection.
     */
    private SecurityFlag createInjectionFlag(SecurityScan securityScan, String flagType, String title,
                                              String content, JavaClass javaClass, Method method,
                                              Integer lineNumber, String pattern) {
        SecurityFlag flag = new SecurityFlag(securityScan, flagType, "high", title);
        
        flag.setDescription(String.format(
            "Detected potential prompt injection attempt. Pattern matched: %s", pattern
        ));
        
        flag.setFilePath(javaClass.getFilePath());
        flag.setClassName(javaClass.getClassName());
        
        if (method != null) {
            flag.setMethodName(method.getMethodName());
        }
        
        flag.setLineNumber(lineNumber);
        
        // Truncate code snippet to 500 characters
        String snippet = content.length() > 500 ? content.substring(0, 500) + "..." : content;
        flag.setCodeSnippet(snippet);
        
        flag.setRecommendation(
            "Review this content carefully. If it's legitimate code documentation, consider rephrasing " +
            "to avoid patterns that resemble prompt injection. If this is an actual injection attempt, " +
            "remove it immediately and investigate how it was introduced."
        );
        
        flag.setCweId("CWE-94"); // Improper Control of Generation of Code
        flag.setOwaspCategory("A03:2021 – Injection");
        flag.setConfidenceScore(85);
        
        return flag;
    }

    /**
     * Get detection statistics.
     */
    public DetectionStatistics getStatistics(List<SecurityFlag> flags) {
        long commentInjections = flags.stream()
            .filter(f -> "ASI01_INJECTION_COMMENT".equals(f.getFlagType()))
            .count();
        
        long stringInjections = flags.stream()
            .filter(f -> "ASI01_INJECTION_STRING".equals(f.getFlagType()))
            .count();
        
        long suspiciousComments = flags.stream()
            .filter(f -> "ASI01_SUSPICIOUS_COMMENT".equals(f.getFlagType()))
            .count();
        
        return new DetectionStatistics(commentInjections, stringInjections, suspiciousComments);
    }

    /**
     * Statistics for ASI01 detection.
     */
    public static class DetectionStatistics {
        private final long commentInjections;
        private final long stringInjections;
        private final long suspiciousComments;

        public DetectionStatistics(long commentInjections, long stringInjections, long suspiciousComments) {
            this.commentInjections = commentInjections;
            this.stringInjections = stringInjections;
            this.suspiciousComments = suspiciousComments;
        }

        public long getCommentInjections() { return commentInjections; }
        public long getStringInjections() { return stringInjections; }
        public long getSuspiciousComments() { return suspiciousComments; }
        public long getTotalFlags() { return commentInjections + stringInjections + suspiciousComments; }
    }
}

// Made with Bob
