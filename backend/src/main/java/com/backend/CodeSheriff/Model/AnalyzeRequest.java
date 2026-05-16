package com.backend.CodeSheriff.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnalyzeRequest {
    
    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String projectName;
    
    @NotBlank(message = "Class name is required")
    @Size(max = 200, message = "Class name must not exceed 200 characters")
    private String className;
    
    @NotBlank(message = "Method name is required")
    @Size(max = 200, message = "Method name must not exceed 200 characters")
    private String methodName;
    
    @NotBlank(message = "Method body is required")
    @Size(max = 50000, message = "Method body must not exceed 50KB")
    private String methodBody;
    
    @Size(max = 500000, message = "Class context must not exceed 500KB")
    private String allClassContext;
}
