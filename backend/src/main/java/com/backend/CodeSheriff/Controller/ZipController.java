package com.backend.CodeSheriff.Controller;

import com.backend.CodeSheriff.Model.ClassInfo;
import com.backend.CodeSheriff.Service.JavaParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:5173")
public class ZipController {

    @Autowired
    private JavaParserService parserService;

    @PostMapping("/upload")
    public ResponseEntity<List<ClassInfo>> uploadZip(
            @RequestParam("file")MultipartFile zipFile) throws IOException{
        if (zipFile.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        Map<String, String> javaFiles = extractJavaFilesFromZip(zipFile);
        List<ClassInfo> classes = parserService.parseAll(javaFiles);

        return ResponseEntity.ok(classes);
    }

    private Map<String, String> extractJavaFilesFromZip(MultipartFile zipFile) throws IOException{
        Map<String, String> result = new HashMap<>();

        try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipFile.getBytes()))) {

            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null){
                if((!entry.isDirectory() && entry.getName().endsWith(".java"))){
                    String content = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    result.put( entry.getName(),content);
                }
                zis.closeEntry();
            }


        }
        return result;
    }

}
