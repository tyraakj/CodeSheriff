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
}
