package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Entity.AuditTrail;
import com.backend.CodeSheriff.Entity.User;
import com.backend.CodeSheriff.Repository.AuditTrailRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for managing audit trail entries.
 * Provides append-only logging of all system actions for compliance and security monitoring.
 * 
 * This is Layer 4 of the security pipeline - all operations are logged here.
 */
@Service
@Transactional
public class AuditTrailService {

    private static final Logger logger = LoggerFactory.getLogger(AuditTrailService.class);

    private final AuditTrailRepository auditTrailRepository;

    public AuditTrailService(AuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }

    /**
     * Log a successful action.
     * @param user the user performing the action
     * @param actionType the type of action
     * @param resourceType the type of resource
     * @param resourceId the resource ID
     * @param request the HTTP request (for IP and user agent)
     * @return the created audit trail entry
     */
    public AuditTrail logSuccess(User user, String actionType, String resourceType, 
                                  UUID resourceId, HttpServletRequest request) {
        return logAction(user, actionType, resourceType, resourceId, "success", null, request, null);
    }

    /**
     * Log a failed action.
     * @param user the user performing the action (can be null for unauthenticated failures)
     * @param actionType the type of action
     * @param resourceType the type of resource
     * @param resourceId the resource ID
     * @param errorMessage the error message
     * @param request the HTTP request (for IP and user agent)
     * @return the created audit trail entry
     */
    public AuditTrail logFailure(User user, String actionType, String resourceType, 
                                  UUID resourceId, String errorMessage, HttpServletRequest request) {
        return logAction(user, actionType, resourceType, resourceId, "failure", errorMessage, request, null);
    }

    /**
     * Log an action with full details.
     * @param user the user performing the action
     * @param actionType the type of action
     * @param resourceType the type of resource
     * @param resourceId the resource ID
     * @param status the status (success, failure, pending)
     * @param errorMessage the error message (if any)
     * @param request the HTTP request (for IP and user agent)
     * @param actionDetails additional details as JSON string
     * @return the created audit trail entry
     */
    public AuditTrail logAction(User user, String actionType, String resourceType, 
                                 UUID resourceId, String status, String errorMessage,
                                 HttpServletRequest request, String actionDetails) {
        try {
            AuditTrail.Builder builder = new AuditTrail.Builder(actionType)
                .user(user)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .status(status)
                .actionDetails(actionDetails);

            if (errorMessage != null) {
                builder.errorMessage(errorMessage);
            }

            if (request != null) {
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }

            AuditTrail auditTrail = builder.build();
            AuditTrail saved = auditTrailRepository.save(auditTrail);
            
            logger.debug("Audit trail created: {} - {} - {}", actionType, status, 
                        user != null ? user.getEmail() : "anonymous");
            
            return saved;
        } catch (Exception e) {
            // Never fail the main operation due to audit logging failure
            logger.error("Failed to create audit trail entry: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Log a timed action (with duration).
     * @param user the user performing the action
     * @param actionType the type of action
     * @param resourceType the type of resource
     * @param resourceId the resource ID
     * @param status the status
     * @param durationMs the duration in milliseconds
     * @param request the HTTP request
     * @return the created audit trail entry
     */
    public AuditTrail logTimedAction(User user, String actionType, String resourceType,
                                      UUID resourceId, String status, long durationMs,
                                      HttpServletRequest request) {
        try {
            AuditTrail.Builder builder = new AuditTrail.Builder(actionType)
                .user(user)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .status(status)
                .durationMs(durationMs);

            if (request != null) {
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }

            AuditTrail auditTrail = builder.build();
            return auditTrailRepository.save(auditTrail);
        } catch (Exception e) {
            logger.error("Failed to create timed audit trail entry: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Log a login attempt.
     * @param email the email address
     * @param success whether the login was successful
     * @param request the HTTP request
     */
    public void logLoginAttempt(String email, boolean success, HttpServletRequest request) {
        try {
            String status = success ? "success" : "failure";
            String actionDetails = String.format("{\"email\":\"%s\"}", email);
            
            AuditTrail.Builder builder = new AuditTrail.Builder("login")
                .status(status)
                .actionDetails(actionDetails);

            if (request != null) {
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"));
            }

            if (!success) {
                builder.errorMessage("Invalid credentials");
            }

            auditTrailRepository.save(builder.build());
            logger.info("Login attempt logged: {} - {}", email, status);
        } catch (Exception e) {
            logger.error("Failed to log login attempt: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a security scan result.
     * @param user the user
     * @param scanType the scan type
     * @param scanId the scan ID
     * @param flagsFound number of flags found
     * @param criticalFlags number of critical flags
     * @param request the HTTP request
     */
    public void logSecurityScan(User user, String scanType, UUID scanId, 
                                 int flagsFound, int criticalFlags, HttpServletRequest request) {
        try {
            String actionDetails = String.format(
                "{\"scanType\":\"%s\",\"flagsFound\":%d,\"criticalFlags\":%d}",
                scanType, flagsFound, criticalFlags
            );
            
            String status = criticalFlags > 0 ? "critical_issues_found" : "success";
            
            logAction(user, "security_scan", "security_scan", scanId, status, null, request, actionDetails);
            logger.info("Security scan logged: {} - {} flags ({} critical)", scanType, flagsFound, criticalFlags);
        } catch (Exception e) {
            logger.error("Failed to log security scan: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a file upload.
     * @param user the user
     * @param filename the filename
     * @param fileSize the file size in bytes
     * @param analysisId the analysis ID
     * @param request the HTTP request
     */
    public void logFileUpload(User user, String filename, long fileSize, 
                               UUID analysisId, HttpServletRequest request) {
        try {
            String actionDetails = String.format(
                "{\"filename\":\"%s\",\"fileSize\":%d}",
                filename, fileSize
            );
            
            logAction(user, "file_upload", "analysis", analysisId, "success", null, request, actionDetails);
            logger.info("File upload logged: {} ({} bytes)", filename, fileSize);
        } catch (Exception e) {
            logger.error("Failed to log file upload: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a Bob AI analysis.
     * @param user the user
     * @param methodId the method ID
     * @param riskLevel the risk level determined
     * @param durationMs the analysis duration
     * @param request the HTTP request
     */
    public void logBobAnalysis(User user, UUID methodId, String riskLevel, 
                                long durationMs, HttpServletRequest request) {
        try {
            String actionDetails = String.format("{\"riskLevel\":\"%s\"}", riskLevel);
            
            logTimedAction(user, "bob_analysis", "method", methodId, "success", durationMs, request);
            logger.info("Bob analysis logged: method {} - {} ({}ms)", methodId, riskLevel, durationMs);
        } catch (Exception e) {
            logger.error("Failed to log Bob analysis: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a file upload with processing time.
     * @param user the user
     * @param analysis the analysis
     * @param filename the filename
     * @param processingTime the processing time in milliseconds
     * @param ipAddress the IP address
     */
    public void logUpload(User user, com.backend.CodeSheriff.Entity.Analysis analysis, 
                          String filename, long processingTime, String ipAddress) {
        try {
            String actionDetails = String.format(
                "{\"filename\":\"%s\",\"processingTime\":%d,\"projectName\":\"%s\"}",
                filename, processingTime, analysis.getProjectName()
            );
            
            AuditTrail.Builder builder = new AuditTrail.Builder("file_upload")
                .user(user)
                .resourceType("analysis")
                .resourceId(analysis.getAnalysisId())
                .status("success")
                .durationMs(processingTime)
                .actionDetails(actionDetails)
                .ipAddress(ipAddress);
            
            auditTrailRepository.save(builder.build());
            logger.info("Upload logged: {} - {} ({}ms)", filename, analysis.getAnalysisId(), processingTime);
        } catch (Exception e) {
            logger.error("Failed to log upload: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a Bob analysis with output type and latency (for analysis-level logging).
     * @param user the user
     * @param analysisId the analysis ID
     * @param outputType the output type
     * @param latencyMs the latency in milliseconds
     * @param request the HTTP request
     */
    public void logBobAnalysisForAnalysis(User user, UUID analysisId, String outputType,
                                long latencyMs, HttpServletRequest request) {
        try {
            String actionDetails = String.format("{\"outputType\":\"%s\"}", outputType);
            
            logTimedAction(user, "bob_analysis", "analysis", analysisId, "success", latencyMs, request);
            logger.info("Bob analysis logged: {} - {} ({}ms)", analysisId, outputType, latencyMs);
        } catch (Exception e) {
            logger.error("Failed to log Bob analysis: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an analysis deletion.
     * @param user the user
     * @param analysis the analysis being deleted
     * @param ipAddress the IP address
     */
    public void logAnalysisDeletion(User user, com.backend.CodeSheriff.Entity.Analysis analysis, String ipAddress) {
        try {
            String actionDetails = String.format(
                "{\"projectName\":\"%s\",\"totalClasses\":%d,\"totalMethods\":%d}",
                analysis.getProjectName(), 
                analysis.getTotalClasses() != null ? analysis.getTotalClasses() : 0,
                analysis.getTotalMethods() != null ? analysis.getTotalMethods() : 0
            );
            
            AuditTrail.Builder builder = new AuditTrail.Builder("analysis_deletion")
                .user(user)
                .resourceType("analysis")
                .resourceId(analysis.getAnalysisId())
                .status("success")
                .actionDetails(actionDetails)
                .ipAddress(ipAddress);
            
            auditTrailRepository.save(builder.build());
            logger.info("Analysis deletion logged: {}", analysis.getAnalysisId());
        } catch (Exception e) {
            logger.error("Failed to log analysis deletion: {}", e.getMessage(), e);
        }
    }

    /**
     * Get client IP address from request, handling proxies.
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Check for suspicious activity from an IP address.
     * @param ipAddress the IP address
     * @param threshold the failure threshold
     * @return true if suspicious activity detected
     */
    @Transactional(readOnly = true)
    public boolean isSuspiciousActivity(String ipAddress, int threshold) {
        long failureCount = auditTrailRepository.countFailuresByIpAddress(ipAddress);
        return failureCount >= threshold;
    }

    /**
     * Get audit trail entry by ID.
     * @param auditId the audit ID
     * @return the audit trail entry
     */
    @Transactional(readOnly = true)
    public AuditTrail getAuditEntry(UUID auditId) {
        return auditTrailRepository.findById(auditId).orElse(null);
    }
}

// Made with Bob
