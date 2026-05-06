package com.backend.CodeSheriff.Model;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassInfo {
    private String className;
    private String filePath;
    private List<String> annotations;
    private List<MethodInfo> methods;
}
