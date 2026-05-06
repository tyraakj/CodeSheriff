package com.backend.CodeSheriff.Model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BobAnalysis {
    private String whatItDoes;
    private String intentVsReality;
    private String whereToStart;
    private boolean hasTests;
    private int lineCount;
}
