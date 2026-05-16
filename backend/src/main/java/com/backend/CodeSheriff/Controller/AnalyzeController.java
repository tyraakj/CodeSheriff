package com.backend.CodeSheriff.Controller;

import com.backend.CodeSheriff.DTO.BobOutputResponseDTO;
import com.backend.CodeSheriff.DTO.MethodResponseDTO;
import com.backend.CodeSheriff.DTO.PagedResponseDTO;
import com.backend.CodeSheriff.DTO.SecurityScanResponseDTO;
import com.backend.CodeSheriff.Entity.*;
import com.backend.CodeSheriff.Model.AnalyzeRequest;
import com.backend.CodeSheriff.Model.BobAnalysis;
import com.backend.CodeSheriff.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for code analysis operations
 * Handles Bob AI analysis and security scanning with full persistence
 *
 * @author CodeSheriff Team
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AnalyzeController {

    @Autowired
    private BobService bobService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityPipelineService securityPipelineService;

    @Autowired
    private AuditTrailService auditTrailService;

    /**
     * Analyze a method using Bob AI with full security pipeline
     * Persists the analysis results and runs all 4 security layers
     *
     * @param request The analysis request containing method details
     * @param userId The authenticated user ID from JWT
     * @param httpRequest HTTP request for IP address extraction
     * @return BobOutputResponseDTO with analysis results
     */
    @PostMapping("/analyze")
    public ResponseEntity<BobOutputResponseDTO> analyzeMethod(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal String userId,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Analysis request from user: {}, IP: {}, class: {}, method: {}",
            userId, ipAddress, request.getClassName(), request.getMethodName());

        // Get user
        User user = userService.getUserBySupabaseId(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            return ResponseEntity.status(401).build();
        }

        // Find the analysis and method
        Analysis analysis = analysisService.getAnalysisByProjectName(user.getId(), request.getProjectName());
        if (analysis == null) {
            log.error("Analysis not found for project: {}", request.getProjectName());
            return ResponseEntity.status(404).build();
        }

        Method method = analysisService.getMethodByName(
            analysis.getId(),
            request.getClassName(),
            request.getMethodName()
        );
        if (method == null) {
            log.error("Method not found: {}.{}", request.getClassName(), request.getMethodName());
            return ResponseEntity.status(404).build();
        }

        try {
            // Run Bob analysis
            BobAnalysis bobAnalysis = bobService.analyze(
                request.getClassName(),
                request.getMethodName(),
                request.getMethodBody(),
                request.getAllClassContext()
            );

            // Create BobOutput entity
            BobOutput bobOutput = new BobOutput(method, user);
            bobOutput.setAnalysisText(bobAnalysis.getAnalysis());
            bobOutput.setComplexityAssessment(bobAnalysis.getComplexityAssessment());
            bobOutput.setTestCoverage(bobAnalysis.getTestCoverage());
            bobOutput.setConfidenceScore((int) Math.round(bobAnalysis.getConfidenceScore()));
            bobOutput.setModelUsed("llama-3-70b-instruct");
            bobOutput.setPromptTokens(bobAnalysis.getPromptTokens());
            bobOutput.setCompletionTokens(bobAnalysis.getCompletionTokens());
            bobOutput.setTotalTokens(bobAnalysis.getTotalTokens());
            bobOutput.setResponseTimeMs(bobAnalysis.getResponseTimeMs());

            // Persist Bob output
            bobOutput = analysisService.addBobOutput(method.getMethodId(), bobOutput);

            // Run full security pipeline
            SecurityPipelineService.SecurityPipelineResult pipelineResult =
                securityPipelineService.runFullPipeline(analysis, user, httpRequest);

            // Log analysis in audit trail
            auditTrailService.logBobAnalysis(
                user,
                method.getMethodId(),
                bobOutput.getRiskLevel(),
                bobOutput.getResponseTimeMs() != null ? bobOutput.getResponseTimeMs() : 0L,
                httpRequest
            );

            log.info("Analysis completed for user: {}, method: {}, security flags: {}",
                userId, request.getMethodName(), pipelineResult.getTotalFlags());

            // Return DTO response
            BobOutputResponseDTO response = BobOutputResponseDTO.fromEntity(bobOutput);
            
            // Add security scan info if available
            if (pipelineResult.getSecurityScan() != null) {
                response.setSecurityScanId(pipelineResult.getSecurityScan().getScanId());
                response.setSecurityFlagCount(pipelineResult.getTotalFlags());
                response.setCriticalFlagCount(pipelineResult.getCriticalFlags());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error analyzing method for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Run security scan on an analysis
     * Executes the full 4-layer security pipeline
     *
     * @param analysisId The analysis ID
     * @param userId The authenticated user ID
     * @param httpRequest HTTP request for IP address extraction
     * @return SecurityScanResponseDTO with scan results
     */
    @PostMapping("/analyses/{analysisId}/security-scan")
    public ResponseEntity<SecurityScanResponseDTO> runSecurityScan(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal String userId,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Security scan request for analysis {} from user: {}, IP: {}",
            analysisId, userId, ipAddress);

        // Get user and analysis
        User user = userService.getUserBySupabaseId(userId);
        Analysis analysis = analysisService.getAnalysisById(analysisId);

        // Verify ownership
        if (!analysis.getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to scan analysis {} owned by another user",
                userId, analysisId);
            return ResponseEntity.status(403).build();
        }

        try {
            // Create analyze request from analysis data
            AnalyzeRequest request = new AnalyzeRequest();
            request.setProjectName(analysis.getProjectName());
            // Add other necessary fields from analysis

            // Run full security pipeline
            SecurityPipelineService.SecurityPipelineResult result =
                securityPipelineService.runFullPipeline(analysis, user, httpRequest);

            log.info("Security scan completed for analysis {}: {} total flags, {} critical",
                analysisId, result.getTotalFlags(), result.getCriticalFlags());

            return ResponseEntity.ok(SecurityScanResponseDTO.fromEntity(result.getSecurityScan()));

        } catch (Exception e) {
            log.error("Error running security scan for analysis {}: {}", analysisId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all Bob analyses for a specific method
     *
     * @param methodId The method ID
     * @param userId The authenticated user ID
     * @param page Page number
     * @param size Page size
     * @return Paginated list of Bob analyses
     */
    @GetMapping("/methods/{methodId}/analyses")
    public ResponseEntity<PagedResponseDTO<BobOutputResponseDTO>> getMethodAnalyses(
            @PathVariable UUID methodId,
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching analyses for method {} by user: {}", methodId, userId);

        Method method = analysisService.getMethodById(methodId);
        
        // Verify ownership
        if (!method.getJavaClass().getAnalysis().getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to access method {} owned by another user",
                userId, methodId);
            return ResponseEntity.status(403).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BobOutput> bobOutputPage = analysisService.getMethodBobOutputs(methodId, pageable);

        List<BobOutputResponseDTO> dtos = bobOutputPage.getContent().stream()
            .map(BobOutputResponseDTO::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponseDTO.fromPage(bobOutputPage, dtos));
    }

    /**
     * Get method details with optional analysis and security flags
     *
     * @param methodId The method ID
     * @param userId The authenticated user ID
     * @param includeAnalysis Include Bob analysis
     * @param includeSecurityFlags Include security flags
     * @return Method details
     */
    @GetMapping("/methods/{methodId}")
    public ResponseEntity<MethodResponseDTO> getMethod(
            @PathVariable UUID methodId,
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "false") boolean includeAnalysis,
            @RequestParam(defaultValue = "false") boolean includeSecurityFlags) {

        log.info("Fetching method {} for user: {}", methodId, userId);

        Method method = analysisService.getMethodById(methodId);
        
        // Verify ownership
        if (!method.getJavaClass().getAnalysis().getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to access method {} owned by another user",
                userId, methodId);
            return ResponseEntity.status(403).build();
        }

        MethodResponseDTO dto = MethodResponseDTO.fromEntity(
            method,
            includeAnalysis,
            includeSecurityFlags
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Get all methods for an analysis with pagination
     *
     * @param analysisId The analysis ID
     * @param userId The authenticated user ID
     * @param page Page number
     * @param size Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction
     * @return Paginated list of methods
     */
    @GetMapping("/analyses/{analysisId}/methods")
    public ResponseEntity<PagedResponseDTO<MethodResponseDTO>> getAnalysisMethods(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "cyclomaticComplexity") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching methods for analysis {} by user: {}", analysisId, userId);

        Analysis analysis = analysisService.getAnalysisById(analysisId);
        
        // Verify ownership
        if (!analysis.getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to access analysis {} owned by another user",
                userId, analysisId);
            return ResponseEntity.status(403).build();
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Method> methodPage = analysisService.getAnalysisMethods(analysisId, pageable);

        List<MethodResponseDTO> dtos = methodPage.getContent().stream()
            .map(method -> MethodResponseDTO.fromEntity(method, false, false))
            .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponseDTO.fromPage(methodPage, dtos));
    }

    /**
     * Get security scans for an analysis
     *
     * @param analysisId The analysis ID
     * @param userId The authenticated user ID
     * @param page Page number
     * @param size Page size
     * @return Paginated list of security scans
     */
    @GetMapping("/analyses/{analysisId}/security-scans")
    public ResponseEntity<PagedResponseDTO<SecurityScanResponseDTO>> getSecurityScans(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching security scans for analysis {} by user: {}", analysisId, userId);

        Analysis analysis = analysisService.getAnalysisById(analysisId);
        
        // Verify ownership
        if (!analysis.getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to access analysis {} owned by another user",
                userId, analysisId);
            return ResponseEntity.status(403).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SecurityScan> scanPage = analysisService.getSecurityScans(analysisId, pageable);

        List<SecurityScanResponseDTO> dtos = scanPage.getContent().stream()
            .map(SecurityScanResponseDTO::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponseDTO.fromPage(scanPage, dtos));
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

