package com.backend.CodeSheriff.Controller;

import com.backend.CodeSheriff.DTO.AnalysisResponseDTO;
import com.backend.CodeSheriff.DTO.PagedResponseDTO;
import com.backend.CodeSheriff.Entity.Analysis;
import com.backend.CodeSheriff.Entity.JavaClass;
import com.backend.CodeSheriff.Entity.Method;
import com.backend.CodeSheriff.Entity.User;
import com.backend.CodeSheriff.Model.ClassInfo;
import com.backend.CodeSheriff.Model.MethodInfo;
import com.backend.CodeSheriff.Service.AnalysisService;
import com.backend.CodeSheriff.Service.AuditTrailService;
import com.backend.CodeSheriff.Service.JavaParserService;
import com.backend.CodeSheriff.Service.UserService;
import com.backend.CodeSheriff.Validation.ZipFileValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * REST Controller for ZIP file upload and analysis management
 * Handles file upload, parsing, and persistence with full audit trail
 *
 * @author CodeSheriff Team
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ZipController {

    @Autowired
    private JavaParserService parserService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private ZipFileValidator zipFileValidator;

    /**
     * Upload and parse a ZIP file containing Java source code
     * Creates a new Analysis and persists all parsed classes and methods
     *
     * @param zipFile The ZIP file containing Java source files
     * @param userId The authenticated user ID from JWT
     * @param request HTTP request for IP address extraction
     * @return AnalysisResponseDTO with parsed data and analysis ID
     */
    @PostMapping("/upload")
    public ResponseEntity<AnalysisResponseDTO> uploadZip(
            @RequestParam("file") @NotNull MultipartFile zipFile,
            @RequestParam(value = "projectName", required = false) String projectName,
            @AuthenticationPrincipal String userId,
            HttpServletRequest request) throws IOException {

        String ipAddress = getClientIpAddress(request);
        log.info("Upload request from user: {}, IP: {}, file: {}, size: {} bytes",
            userId, ipAddress, zipFile.getOriginalFilename(), zipFile.getSize());

        // Validate ZIP file
        zipFileValidator.validate(zipFile);

        // Get or create user
        User user = userService.getOrCreateUser(userId, null);

        // Create analysis
        String fileName = zipFile.getOriginalFilename();
        String analysisProjectName = projectName != null ? projectName :
            fileName.substring(0, fileName.lastIndexOf('.'));
        
        Analysis analysis = analysisService.createAnalysis(
            user.getId(),
            analysisProjectName,
            fileName
        );

        // Log upload in audit trail
        auditTrailService.logUpload(user, analysis, fileName, zipFile.getSize(), ipAddress);

        try {
            // Start analysis
            analysisService.startAnalysis(analysis.getId());

            // Extract and parse Java files
            Map<String, String> javaFiles = extractJavaFilesFromZip(zipFile);
            List<ClassInfo> parsedClasses = parserService.parseAll(javaFiles);

            log.info("Parsed {} classes from {} Java files", parsedClasses.size(), javaFiles.size());

            // Persist all classes and methods
            for (ClassInfo classInfo : parsedClasses) {
                JavaClass javaClass = convertToEntity(classInfo, analysis);
                analysisService.addJavaClass(analysis.getId(), javaClass);

                // Add methods
                for (MethodInfo methodInfo : classInfo.getMethods()) {
                    Method method = convertToEntity(methodInfo, javaClass);
                    analysisService.addMethod(javaClass.getClassId(), method);
                }
            }

            // Complete analysis
            analysisService.completeAnalysis(analysis.getId());

            log.info("Successfully persisted analysis {} with {} classes for user: {}",
                analysis.getId(), parsedClasses.size(), userId);

            // Return DTO response
            Analysis completedAnalysis = analysisService.getAnalysisById(analysis.getId());
            return ResponseEntity.ok(AnalysisResponseDTO.fromEntity(completedAnalysis));

        } catch (Exception e) {
            log.error("Error processing ZIP file for user {}: {}", userId, e.getMessage(), e);
            analysisService.failAnalysis(analysis.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Get all analyses for the authenticated user with pagination
     *
     * @param userId The authenticated user ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of analyses
     */
    @GetMapping("/analyses")
    public ResponseEntity<PagedResponseDTO<AnalysisResponseDTO>> getUserAnalyses(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching analyses for user: {}, page: {}, size: {}", userId, page, size);

        User user = userService.getUserBySupabaseId(userId);
        if (user == null) {
            return ResponseEntity.ok(PagedResponseDTO.empty());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Analysis> analysisPage = analysisService.getUserAnalyses(user.getId(), pageable);
        
        List<AnalysisResponseDTO> dtos = analysisPage.getContent().stream()
            .map(AnalysisResponseDTO::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponseDTO.fromPage(analysisPage, dtos));
    }

    /**
     * Get a specific analysis by ID
     *
     * @param analysisId The analysis ID
     * @param userId The authenticated user ID
     * @return Analysis details
     */
    @GetMapping("/analyses/{analysisId}")
    public ResponseEntity<AnalysisResponseDTO> getAnalysis(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal String userId) {

        log.info("Fetching analysis {} for user: {}", analysisId, userId);

        Analysis analysis = analysisService.getAnalysisById(analysisId);
        
        // Verify ownership
        if (!analysis.getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to access analysis {} owned by another user",
                userId, analysisId);
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(AnalysisResponseDTO.fromEntity(analysis));
    }

    /**
     * Delete an analysis
     *
     * @param analysisId The analysis ID
     * @param userId The authenticated user ID
     * @return No content on success
     */
    @DeleteMapping("/analyses/{analysisId}")
    public ResponseEntity<Void> deleteAnalysis(
            @PathVariable UUID analysisId,
            @AuthenticationPrincipal String userId,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        log.info("Delete request for analysis {} from user: {}, IP: {}",
            analysisId, userId, ipAddress);

        Analysis analysis = analysisService.getAnalysisById(analysisId);
        
        // Verify ownership
        if (!analysis.getUser().getUserId().toString().equals(userId)) {
            log.warn("User {} attempted to delete analysis {} owned by another user",
                userId, analysisId);
            return ResponseEntity.status(403).build();
        }

        // Log deletion
        auditTrailService.logAnalysisDeletion(
            analysis.getUser(),
            analysis,
            ipAddress
        );

        analysisService.deleteAnalysis(analysisId);

        log.info("Successfully deleted analysis {} for user: {}", analysisId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extract Java files from ZIP
     */
    private Map<String, String> extractJavaFilesFromZip(MultipartFile zipFile) throws IOException {
        Map<String, String> result = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                    String content = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    result.put(entry.getName(), content);
                }
                zis.closeEntry();
            }
        }

        return result;
    }

    /**
     * Convert ClassInfo to JavaClass entity
     */
    private JavaClass convertToEntity(ClassInfo classInfo, Analysis analysis) {
        JavaClass javaClass = new JavaClass(analysis, classInfo.getClassName());
        javaClass.setPackageName(classInfo.getPackageName());
        javaClass.setIsAbstract(classInfo.getIsAbstract());
        javaClass.setIsInterface("interface".equalsIgnoreCase(classInfo.getClassType()));
        javaClass.setIsEnum("enum".equalsIgnoreCase(classInfo.getClassType()));
        javaClass.setExtendsClass(classInfo.getExtendedClass());
        javaClass.setImplementsInterfaces(splitCsv(classInfo.getImplementedInterfaces()));
        javaClass.setAnnotations(classInfo.getAnnotations());
        javaClass.setSourceCode(classInfo.getSourceCode());
        return javaClass;
    }

    /**
     * Convert MethodInfo to Method entity
     */
    private Method convertToEntity(MethodInfo methodInfo, JavaClass javaClass) {
        Method method = new Method(javaClass, methodInfo.getMethodName());
        method.setReturnType(methodInfo.getReturnType());
        method.setParameters(splitCsv(methodInfo.getParameters()));
        method.setVisibility(methodInfo.getVisibility());
        method.setIsStatic(methodInfo.getIsStatic());
        method.setIsFinal(methodInfo.getIsFinal());
        method.setIsAbstract(methodInfo.getIsAbstract());
        method.setIsSynchronized(methodInfo.getIsSynchronized());
        method.setIsConstructor(methodInfo.getIsConstructor());
        method.setLineStart(methodInfo.getLineStart());
        method.setLineEnd(methodInfo.getLineEnd());
        method.setLinesOfCode(methodInfo.getLinesOfCode());
        method.setCyclomaticComplexity(methodInfo.getCyclomaticComplexity());
        method.setCognitiveComplexity(methodInfo.getCognitiveComplexity());
        method.setParameterCount(methodInfo.getParameterCount());
        method.setLocalVariableCount(methodInfo.getLocalVariableCount());
        method.setSourceCode(methodInfo.getSourceCode());
        method.setJavadoc(methodInfo.getJavadoc());
        method.setAnnotations(splitCsv(methodInfo.getAnnotations()));
        method.setThrowsExceptions(splitCsv(methodInfo.getThrownExceptions()));
        method.setCalledMethods(methodInfo.getCalledMethods());
        method.setHasLoops(methodInfo.getHasLoops());
        method.setHasConditionals(methodInfo.getHasConditionals());
        method.setHasTryCatch(methodInfo.getHasTryCatch());
        return method;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toList());
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
