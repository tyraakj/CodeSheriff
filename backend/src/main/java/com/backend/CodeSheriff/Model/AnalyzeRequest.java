package com.backend.CodeSheriff.Model;

import lombok.Data;

@Data
public class AnalyzeRequest {
    private String className;
    private String methodName;
    private String methodBody;
    private String allClassContext;
}
