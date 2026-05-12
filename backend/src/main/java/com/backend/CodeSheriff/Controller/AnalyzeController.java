package com.backend.CodeSheriff.Controller;

import com.backend.CodeSheriff.Model.AnalyzeRequest;
import com.backend.CodeSheriff.Model.BobAnalysis;
import com.backend.CodeSheriff.Service.BobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@CrossOrigin("http://localhost:5173")
public class AnalyzeController {

    @Autowired
    private BobService bobService;

    @PostMapping("/analyze")
    public ResponseEntity<BobAnalysis> analyzeMethod(@RequestBody AnalyzeRequest request){

        BobAnalysis analysis = bobService.analyze(
                request.getClassName(),
                request.getMethodName(),
                request.getMethodBody(),
                request.getAllClassContext()
        );
        return ResponseEntity.ok(analysis);
    }
}

