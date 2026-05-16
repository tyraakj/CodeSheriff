package com.backend.CodeSheriff.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfo {

    private String className;
    private String filePath;
    private List<String> annotations;
    private List<MethodInfo> methods;
    
    // Additional fields for DTOs and controllers
    private String packageName;
    private String fullyQualifiedName;
    private String classType;
    private String modifiers;
    private Boolean isAbstract;
    private Boolean isFinal;
    private Boolean isStatic;
    private String extendedClass;
    private String implementedInterfaces;
    private String fields;
    private String sourceCode;
    private String javadoc;
}
