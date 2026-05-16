package com.backend.CodeSheriff.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JavaClass entity representing a parsed Java class from uploaded code.
 * Maps to the 'java_classes' table in Supabase PostgreSQL.
 * 
 * Each JavaClass belongs to an Analysis and contains multiple Methods.
 */
@Entity
@Table(name = "java_classes", schema = "public", indexes = {
    @Index(name = "idx_java_classes_analysis_id", columnList = "analysis_id"),
    @Index(name = "idx_java_classes_package_name", columnList = "package_name"),
    @Index(name = "idx_java_classes_class_name", columnList = "class_name")
})
public class JavaClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "class_id", nullable = false, updatable = false)
    private UUID classId;

    @NotNull(message = "Analysis is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, foreignKey = @ForeignKey(name = "fk_java_classes_analysis"))
    private Analysis analysis;

    @NotBlank(message = "Class name is required")
    @Size(max = 255, message = "Class name must not exceed 255 characters")
    @Column(name = "class_name", nullable = false, length = 255)
    private String className;

    @Size(max = 255, message = "Package name must not exceed 255 characters")
    @Column(name = "package_name", length = 255)
    private String packageName;

    @Size(max = 500, message = "File path must not exceed 500 characters")
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "is_interface")
    private Boolean isInterface = false;

    @Column(name = "is_abstract")
    private Boolean isAbstract = false;

    @Column(name = "is_enum")
    private Boolean isEnum = false;

    @Size(max = 255, message = "Extends class must not exceed 255 characters")
    @Column(name = "extends_class", length = 255)
    private String extendsClass;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "java_class_implements",
        joinColumns = @JoinColumn(name = "class_id", foreignKey = @ForeignKey(name = "fk_class_implements_class"))
    )
    @Column(name = "interface_name", length = 255)
    private List<String> implementsInterfaces = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "java_class_annotations",
        joinColumns = @JoinColumn(name = "class_id", foreignKey = @ForeignKey(name = "fk_class_annotations_class"))
    )
    @Column(name = "annotation_name", length = 255)
    private List<String> annotations = new ArrayList<>();

    @Column(name = "line_count")
    private Integer lineCount;

    @Column(name = "method_count")
    private Integer methodCount = 0;

    @Column(name = "field_count")
    private Integer fieldCount = 0;

    @Column(name = "source_code", columnDefinition = "TEXT")
    private String sourceCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "javaClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Method> methods = new ArrayList<>();

    // Constructors
    public JavaClass() {
    }

    public JavaClass(Analysis analysis, String className) {
        this.analysis = analysis;
        this.className = className;
    }

    // Getters and Setters
    public UUID getClassId() {
        return classId;
    }

    public void setClassId(UUID classId) {
        this.classId = classId;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Boolean getIsInterface() {
        return isInterface;
    }

    public void setIsInterface(Boolean isInterface) {
        this.isInterface = isInterface;
    }

    public Boolean getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public Boolean getIsEnum() {
        return isEnum;
    }

    public void setIsEnum(Boolean isEnum) {
        this.isEnum = isEnum;
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    public List<String> getImplementsInterfaces() {
        return implementsInterfaces;
    }

    public void setImplementsInterfaces(List<String> implementsInterfaces) {
        this.implementsInterfaces = implementsInterfaces;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }

    public Integer getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(Integer methodCount) {
        this.methodCount = methodCount;
    }

    public Integer getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    // Helper methods for bidirectional relationships
    public void addMethod(Method method) {
        methods.add(method);
        method.setJavaClass(this);
    }

    public void removeMethod(Method method) {
        methods.remove(method);
        method.setJavaClass(null);
    }

    // Business logic helpers
    public String getFullyQualifiedName() {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName + "." + className;
        }
        return className;
    }

    public void incrementMethodCount() {
        this.methodCount = (this.methodCount == null ? 0 : this.methodCount) + 1;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaClass)) return false;
        JavaClass javaClass = (JavaClass) o;
        return classId != null && classId.equals(javaClass.getClassId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "JavaClass{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", isInterface=" + isInterface +
                ", isAbstract=" + isAbstract +
                ", methodCount=" + methodCount +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
