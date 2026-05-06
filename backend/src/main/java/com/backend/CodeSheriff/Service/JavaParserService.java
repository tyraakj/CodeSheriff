package com.backend.CodeSheriff.Service;
import com.backend.CodeSheriff.Model.MethodInfo;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;
import com.backend.CodeSheriff.Model.ClassInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JavaParserService {
    public List<ClassInfo> parseAll(Map<String,String> javaFiles){
        List<ClassInfo> result = new ArrayList<>();

        for(Map.Entry<String, String > entry: javaFiles.entrySet()){
            String filePath = entry.getKey();
            String content = entry.getValue();

            try {
                List <ClassInfo> classesInFile = parseOneFile(filePath, content);
                result.addAll(classesInFile);

            } catch (Exception e) {
                log.error("Failed to parse Java file: {}" , filePath, e);
            }
        }
        return result;
        }

    private List<ClassInfo> parseOneFile(String filePath, String content){

        CompilationUnit cu = StaticJavaParser.parse(content);
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        List<ClassInfo> classInfos = new ArrayList<>();

        for(ClassOrInterfaceDeclaration cls: classes){

            List<String> annotations = cls.getAnnotations().stream()
                    .map(a -> a.getNameAsString())
                    .collect(Collectors.toList());

            List<MethodInfo> methods = cls.getMethods().stream()
                       .map(this::extractMethodInfo)
                       .collect(Collectors.toList());

            ClassInfo info = ClassInfo.builder()
                    .className(cls.getNameAsString())
                    .filePath(filePath)
                    .annotations(annotations)
                    .methods(methods)
                    .build();

            classInfos.add(info);

        }
        return classInfos;
    }

    private MethodInfo extractMethodInfo(MethodDeclaration method){
        String body = method.getBody()
                .map(Node::toString)
                .orElse("//no body");

        String params = method.getParameters().stream()
                .map(p -> p.getTypeAsString() + " " + p.getNameAsString())
                .collect(Collectors.joining(", "));

        int lineStart = method.getBegin()
                .map(pos -> pos.line)
                .orElse(0);

        return MethodInfo.builder()
                .name(method.getNameAsString())
                .returnType(method.getTypeAsString())
                .visibility(method.getAccessSpecifier().asString())
                .signature(method.getDeclarationAsString())
                .params(params)
                .lineStart(lineStart)
                .body(body)
                .build();
    }
    }


