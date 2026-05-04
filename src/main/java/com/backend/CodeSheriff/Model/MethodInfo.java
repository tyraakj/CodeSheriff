package com.backend.CodeSheriff.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MethodInfo {
    private String name;
    private String signature;
    private String returnType;
    private String visibility;
    private int lineStart;
    private String body;
    private String params;
}
