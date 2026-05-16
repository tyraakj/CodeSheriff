package com.backend.CodeSheriff.Security;

import com.backend.CodeSheriff.Entity.JavaClass;
import com.backend.CodeSheriff.Entity.Method;
import com.backend.CodeSheriff.Entity.SecurityFlag;
import com.backend.CodeSheriff.Entity.SecurityScan;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Layer 2 of Security Pipeline: Credential Leak Detection
 * 
 * Detects hardcoded credentials, API keys, tokens, and other secrets in source code.
 * Scans for:
 * - API keys and tokens
 * - Passwords and secrets
 * - Database connection strings
 * - Private keys and certificates
 * - AWS/Azure/GCP credentials
 * - OAuth tokens
 */
@Component
public class CredentialLeakDetector {

    private static final Logger logger = LoggerFactory.getLogger(CredentialLeakDetector.class);

    // Credential patterns
    private static final List<CredentialPattern> CREDENTIAL_PATTERNS = List.of(
        // Generic API keys
        new CredentialPattern(
            "GENERIC_API_KEY",
            Pattern.compile("(?i)(api[_-]?key|apikey|api[_-]?secret)\\s*[=:]\\s*['\"]([a-zA-Z0-9_\\-]{20,})['\"]"),
            "critical",
            "Hardcoded API key detected"
        ),
        
        // AWS credentials
        new CredentialPattern(
            "AWS_ACCESS_KEY",
            Pattern.compile("(?i)(aws[_-]?access[_-]?key[_-]?id|aws[_-]?secret[_-]?access[_-]?key)\\s*[=:]\\s*['\"]([A-Z0-9]{20,})['\"]"),
            "critical",
            "AWS credential detected"
        ),
        new CredentialPattern(
            "AWS_SECRET_KEY",
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            "critical",
            "AWS Access Key ID detected"
        ),
        
        // Generic passwords
        new CredentialPattern(
            "PASSWORD",
            Pattern.compile("(?i)(password|passwd|pwd)\\s*[=:]\\s*['\"]([^'\"]{8,})['\"]"),
            "high",
            "Hardcoded password detected"
        ),
        
        // Database connection strings
        new CredentialPattern(
            "DB_CONNECTION_STRING",
            Pattern.compile("(?i)(jdbc:|mongodb:|postgresql:|mysql:)//[^\\s]+:[^\\s]+@"),
            "critical",
            "Database connection string with credentials"
        ),
        
        // Private keys
        new CredentialPattern(
            "PRIVATE_KEY",
            Pattern.compile("-----BEGIN (RSA |EC |DSA )?PRIVATE KEY-----"),
            "critical",
            "Private key detected in code"
        ),
        
        // OAuth tokens
        new CredentialPattern(
            "OAUTH_TOKEN",
            Pattern.compile("(?i)(oauth[_-]?token|access[_-]?token|bearer[_-]?token)\\s*[=:]\\s*['\"]([a-zA-Z0-9_\\-\\.]{20,})['\"]"),
            "high",
            "OAuth/Bearer token detected"
        ),
        
        // GitHub tokens
        new CredentialPattern(
            "GITHUB_TOKEN",
            Pattern.compile("gh[pousr]_[A-Za-z0-9_]{36,}"),
            "critical",
            "GitHub token detected"
        ),
        
        // Slack tokens
        new CredentialPattern(
            "SLACK_TOKEN",
            Pattern.compile("xox[baprs]-[0-9]{10,13}-[0-9]{10,13}-[a-zA-Z0-9]{24,}"),
            "high",
            "Slack token detected"
        ),
        
        // Google API keys
        new CredentialPattern(
            "GOOGLE_API_KEY",
            Pattern.compile("AIza[0-9A-Za-z\\-_]{35}"),
            "critical",
            "Google API key detected"
        ),
        
        // Azure credentials
        new CredentialPattern(
            "AZURE_KEY",
            Pattern.compile("(?i)(azure[_-]?key|azure[_-]?secret)\\s*[=:]\\s*['\"]([a-zA-Z0-9+/=]{40,})['\"]"),
            "critical",
            "Azure credential detected"
        ),
        
        // JWT tokens
        new CredentialPattern(
            "JWT_TOKEN",
            Pattern.compile("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}"),
            "high",
            "JWT token detected"
        ),
        
        // Generic secrets
        new CredentialPattern(
            "SECRET",
            Pattern.compile("(?i)(secret|token|key)\\s*[=:]\\s*['\"]([a-zA-Z0-9_\\-]{32,})['\"]"),
            "medium",
            "Potential secret detected"
        )
    );

    // Variable names that suggest credentials
    private static final List<String> SUSPICIOUS_VARIABLE_NAMES = List.of(
        "password", "passwd", "pwd", "secret", "token", "apikey", "api_key",
        "access_key", "private_key", "auth_token", "bearer_token", "oauth_token",
        "client_secret", "api_secret", "db_password", "database_password"
    );

    /**
     * Scan a Java class for credential leaks.
     */
    public List<SecurityFlag> scanJavaClass(JavaClass javaClass, CompilationUnit compilationUnit,
                                             SecurityScan securityScan) {
        logger.debug("Scanning class {} for credential leaks", javaClass.getClassName());
        
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Scan field declarations
        compilationUnit.findAll(FieldDeclaration.class).forEach(field -> {
            List<SecurityFlag> fieldFlags = scanFieldDeclaration(field, javaClass, securityScan);
            flags.addAll(fieldFlags);
        });
        
        // Scan all string literals
        compilationUnit.findAll(StringLiteralExpr.class).forEach(stringLiteral -> {
            List<SecurityFlag> stringFlags = scanStringLiteral(stringLiteral, javaClass, null, securityScan);
            flags.addAll(stringFlags);
        });
        
        if (!flags.isEmpty()) {
            logger.warn("Found {} potential credential leaks in class {}", 
                       flags.size(), javaClass.getClassName());
        }
        
        return flags;
    }

    /**
     * Scan a method for credential leaks.
     */
    public List<SecurityFlag> scanMethod(Method method, com.github.javaparser.ast.body.MethodDeclaration methodNode,
                                          JavaClass javaClass, SecurityScan securityScan) {
        logger.debug("Scanning method {} for credential leaks", method.getMethodName());
        
        List<SecurityFlag> flags = new ArrayList<>();
        
        // Scan variable declarations and assignments
        methodNode.findAll(VariableDeclarator.class).forEach(variable -> {
            List<SecurityFlag> varFlags = scanVariableDeclarator(variable, javaClass, method, securityScan);
            flags.addAll(varFlags);
        });
        
        // Scan assignments
        methodNode.findAll(AssignExpr.class).forEach(assignment -> {
            List<SecurityFlag> assignFlags = scanAssignment(assignment, javaClass, method, securityScan);
            flags.addAll(assignFlags);
        });
        
        // Scan string literals
        methodNode.findAll(StringLiteralExpr.class).forEach(stringLiteral -> {
            List<SecurityFlag> stringFlags = scanStringLiteral(stringLiteral, javaClass, method, securityScan);
            flags.addAll(stringFlags);
        });
        
        return flags;
    }

    /**
     * Scan a field declaration for credentials.
     */
    private List<SecurityFlag> scanFieldDeclaration(FieldDeclaration field, JavaClass javaClass,
                                                      SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        field.getVariables().forEach(variable -> {
            String varName = variable.getNameAsString().toLowerCase();
            
            // Check if variable name suggests credentials
            if (SUSPICIOUS_VARIABLE_NAMES.stream().anyMatch(varName::contains)) {
                variable.getInitializer().ifPresent(init -> {
                    if (init instanceof StringLiteralExpr) {
                        String value = ((StringLiteralExpr) init).getValue();
                        if (!value.isEmpty() && !isPlaceholder(value)) {
                            SecurityFlag flag = createCredentialFlag(
                                securityScan,
                                "SUSPICIOUS_FIELD",
                                "Field with credential-like name contains hardcoded value",
                                variable.getNameAsString(),
                                value,
                                javaClass,
                                null,
                                field.getRange().map(r -> r.begin.line).orElse(null)
                            );
                            flags.add(flag);
                        }
                    }
                });
            }
        });
        
        return flags;
    }

    /**
     * Scan a variable declarator for credentials.
     */
    private List<SecurityFlag> scanVariableDeclarator(VariableDeclarator variable, JavaClass javaClass,
                                                        Method method, SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        String varName = variable.getNameAsString().toLowerCase();
        
        if (SUSPICIOUS_VARIABLE_NAMES.stream().anyMatch(varName::contains)) {
            variable.getInitializer().ifPresent(init -> {
                if (init instanceof StringLiteralExpr) {
                    String value = ((StringLiteralExpr) init).getValue();
                    if (!value.isEmpty() && !isPlaceholder(value)) {
                        SecurityFlag flag = createCredentialFlag(
                            securityScan,
                            "SUSPICIOUS_VARIABLE",
                            "Variable with credential-like name contains hardcoded value",
                            variable.getNameAsString(),
                            value,
                            javaClass,
                            method,
                            variable.getRange().map(r -> r.begin.line).orElse(null)
                        );
                        flags.add(flag);
                    }
                }
            });
        }
        
        return flags;
    }

    /**
     * Scan an assignment for credentials.
     */
    private List<SecurityFlag> scanAssignment(AssignExpr assignment, JavaClass javaClass,
                                                Method method, SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        
        if (assignment.getValue() instanceof StringLiteralExpr) {
            String targetName = assignment.getTarget().toString().toLowerCase();
            
            if (SUSPICIOUS_VARIABLE_NAMES.stream().anyMatch(targetName::contains)) {
                String value = ((StringLiteralExpr) assignment.getValue()).getValue();
                if (!value.isEmpty() && !isPlaceholder(value)) {
                    SecurityFlag flag = createCredentialFlag(
                        securityScan,
                        "SUSPICIOUS_ASSIGNMENT",
                        "Assignment to credential-like variable with hardcoded value",
                        assignment.getTarget().toString(),
                        value,
                        javaClass,
                        method,
                        assignment.getRange().map(r -> r.begin.line).orElse(null)
                    );
                    flags.add(flag);
                }
            }
        }
        
        return flags;
    }

    /**
     * Scan a string literal for credential patterns.
     */
    private List<SecurityFlag> scanStringLiteral(StringLiteralExpr stringLiteral, JavaClass javaClass,
                                                   Method method, SecurityScan securityScan) {
        List<SecurityFlag> flags = new ArrayList<>();
        String value = stringLiteral.getValue();
        
        // Check each credential pattern
        for (CredentialPattern pattern : CREDENTIAL_PATTERNS) {
            if (pattern.pattern.matcher(value).find()) {
                SecurityFlag flag = createPatternFlag(
                    securityScan,
                    pattern,
                    value,
                    javaClass,
                    method,
                    stringLiteral.getRange().map(r -> r.begin.line).orElse(null)
                );
                flags.add(flag);
            }
        }
        
        return flags;
    }

    /**
     * Check if a value is a placeholder (not a real credential).
     */
    private boolean isPlaceholder(String value) {
        String lower = value.toLowerCase();
        return lower.contains("placeholder") ||
               lower.contains("example") ||
               lower.contains("your_") ||
               lower.contains("xxx") ||
               lower.contains("***") ||
               lower.equals("password") ||
               lower.equals("secret") ||
               lower.equals("token") ||
               value.length() < 8;
    }

    /**
     * Create a security flag for credential detection.
     */
    private SecurityFlag createCredentialFlag(SecurityScan securityScan, String flagType, String title,
                                                String variableName, String value, JavaClass javaClass,
                                                Method method, Integer lineNumber) {
        SecurityFlag flag = new SecurityFlag(securityScan, flagType, "high", title);
        
        flag.setDescription(String.format(
            "Variable '%s' contains a hardcoded value that appears to be a credential or secret.",
            variableName
        ));
        
        flag.setFilePath(javaClass.getFilePath());
        flag.setClassName(javaClass.getClassName());
        
        if (method != null) {
            flag.setMethodName(method.getMethodName());
        }
        
        flag.setLineNumber(lineNumber);
        
        // Mask the actual value in the snippet
        String maskedValue = value.length() > 4 ? 
            value.substring(0, 2) + "***" + value.substring(value.length() - 2) : "***";
        flag.setCodeSnippet(String.format("%s = \"%s\"", variableName, maskedValue));
        
        flag.setRecommendation(
            "Never hardcode credentials in source code. Use environment variables, " +
            "configuration files (excluded from version control), or secret management services " +
            "like AWS Secrets Manager, Azure Key Vault, or HashiCorp Vault."
        );
        
        flag.setCweId("CWE-798"); // Use of Hard-coded Credentials
        flag.setOwaspCategory("A07:2021 – Identification and Authentication Failures");
        flag.setConfidenceScore(90);
        
        return flag;
    }

    /**
     * Create a security flag for pattern-based detection.
     */
    private SecurityFlag createPatternFlag(SecurityScan securityScan, CredentialPattern pattern,
                                            String value, JavaClass javaClass, Method method,
                                            Integer lineNumber) {
        SecurityFlag flag = new SecurityFlag(securityScan, pattern.type, pattern.severity, pattern.title);
        
        flag.setDescription(String.format(
            "Detected %s in source code. This appears to be a hardcoded credential.",
            pattern.title.toLowerCase()
        ));
        
        flag.setFilePath(javaClass.getFilePath());
        flag.setClassName(javaClass.getClassName());
        
        if (method != null) {
            flag.setMethodName(method.getMethodName());
        }
        
        flag.setLineNumber(lineNumber);
        
        // Mask sensitive parts
        String maskedValue = value.length() > 10 ? 
            value.substring(0, 5) + "***" + value.substring(value.length() - 5) : "***";
        flag.setCodeSnippet(maskedValue);
        
        flag.setRecommendation(
            "Remove this hardcoded credential immediately. Use environment variables or " +
            "a secure secret management solution. Rotate the exposed credential if it's still in use."
        );
        
        flag.setCweId("CWE-798");
        flag.setOwaspCategory("A07:2021 – Identification and Authentication Failures");
        flag.setConfidenceScore(95);
        
        return flag;
    }

    /**
     * Pattern definition for credential detection.
     */
    private static class CredentialPattern {
        final String type;
        final Pattern pattern;
        final String severity;
        final String title;

        CredentialPattern(String type, Pattern pattern, String severity, String title) {
            this.type = type;
            this.pattern = pattern;
            this.severity = severity;
            this.title = title;
        }
    }

    /**
     * Get detection statistics.
     */
    public DetectionStatistics getStatistics(List<SecurityFlag> flags) {
        long criticalLeaks = flags.stream()
            .filter(f -> "critical".equals(f.getSeverity()))
            .count();
        
        long highLeaks = flags.stream()
            .filter(f -> "high".equals(f.getSeverity()))
            .count();
        
        long mediumLeaks = flags.stream()
            .filter(f -> "medium".equals(f.getSeverity()))
            .count();
        
        return new DetectionStatistics(criticalLeaks, highLeaks, mediumLeaks);
    }

    /**
     * Statistics for credential leak detection.
     */
    public static class DetectionStatistics {
        private final long criticalLeaks;
        private final long highLeaks;
        private final long mediumLeaks;

        public DetectionStatistics(long criticalLeaks, long highLeaks, long mediumLeaks) {
            this.criticalLeaks = criticalLeaks;
            this.highLeaks = highLeaks;
            this.mediumLeaks = mediumLeaks;
        }

        public long getCriticalLeaks() { return criticalLeaks; }
        public long getHighLeaks() { return highLeaks; }
        public long getMediumLeaks() { return mediumLeaks; }
        public long getTotalLeaks() { return criticalLeaks + highLeaks + mediumLeaks; }
    }
}

// Made with Bob
