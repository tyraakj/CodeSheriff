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
}