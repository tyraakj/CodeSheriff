package com.backend.CodeSheriff.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodInfo {

    private String name;
    private String signature;
    private String returnType;
    private String visibility;
    private int lineStart;
    private String body;
    private String params;
    
    // Additional fields for DTOs and controllers
    private String methodName;
    private String parameters;
    private String modifiers;
    private Boolean isStatic;
    private Boolean isFinal;
    private Boolean isAbstract;
    private Boolean isSynchronized;
    private Boolean isConstructor;
    private Integer lineEnd;
    private Integer linesOfCode;
    private Integer cyclomaticComplexity;
    private Integer cognitiveComplexity;
    private Integer parameterCount;
    private Integer localVariableCount;
    private String sourceCode;
    private String javadoc;
    private String annotations;
    private String thrownExceptions;
    private String calledMethods;
    private Boolean hasLoops;
    private Boolean hasConditionals;
    private Boolean hasTryCatch;
}