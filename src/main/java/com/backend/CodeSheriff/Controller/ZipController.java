package com.backend.CodeSheriff.Controller;

import com.backend.CodeSheriff.Service.JavaParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:5173")
public class ZipController {

    @Autowired
    private JavaParserService parserService;
}
