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
 * Method entity representing a parsed method from a Java class.
 * Maps to the 'methods' table in Supabase PostgreSQL.
 * 
 * Each Method belongs to a JavaClass and can have one BobOutput (AI analysis).
 */
@Entity
@Table(name = "methods", schema = "public", indexes = {
    @Index(name = "idx_methods_class_id", columnList = "class_id"),
    @Index(name = "idx_methods_method_name", columnList = "method_name"),
    @Index(name = "idx_methods_visibility", columnList = "visibility")
})
public class Method {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "method_id", nullable = false, updatable = false)
    private UUID methodId;

    @NotNull(message = "Java class is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_methods_class"))
    private JavaClass javaClass;

    @NotBlank(message = "Method name is required")
    @Size(max = 255, message = "Method name must not exceed 255 characters")
    @Column(name = "method_name", nullable = false, length = 255)
    private String methodName;

    @Size(max = 255, message = "Return type must not exceed 255 characters")
    @Column(name = "return_type", length = 255)
    private String returnType;

    @Size(max = 50, message = "Visibility must not exceed 50 characters")
    @Column(name = "visibility", length = 50)
    private String visibility; // public, private, protected, package-private

    @Column(name = "is_static")
    private Boolean isStatic = false;

    @Column(name = "is_abstract")
    private Boolean isAbstract = false;

    @Column(name = "is_final")
    private Boolean isFinal = false;

    @Column(name = "is_synchronized")
    private Boolean isSynchronized = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "method_parameters",
        joinColumns = @JoinColumn(name = "method_id", foreignKey = @ForeignKey(name = "fk_method_parameters_method"))
    )
    @Column(name = "parameter", length = 255)
    private List<String> parameters = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "method_annotations",
        joinColumns = @JoinColumn(name = "method_id", foreignKey = @ForeignKey(name = "fk_method_annotations_method"))
    )
    @Column(name = "annotation_name", length = 255)
    private List<String> annotations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "method_exceptions",
        joinColumns = @JoinColumn(name = "method_id", foreignKey = @ForeignKey(name = "fk_method_exceptions_method"))
    )
    @Column(name = "exception_type", length = 255)
    private List<String> throwsExceptions = new ArrayList<>();

    @Column(name = "line_count")
    private Integer lineCount;

    @Column(name = "cyclomatic_complexity")
    private Integer cyclomaticComplexity;

    @Column(name = "has_javadoc")
    private Boolean hasJavadoc = false;

    @Column(name = "source_code", columnDefinition = "TEXT")
    private String sourceCode;

    @Column(name = "javadoc", columnDefinition = "TEXT")
    private String javadoc;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Relationships
    @OneToOne(mappedBy = "method", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BobOutput bobOutput;

    // Constructors
    public Method() {
    }

    public Method(JavaClass javaClass, String methodName) {
        this.javaClass = javaClass;
        this.methodName = methodName;
    }

    // Getters and Setters
    public UUID getMethodId() {
        return methodId;
    }

    public void setMethodId(UUID methodId) {
        this.methodId = methodId;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(JavaClass javaClass) {
        this.javaClass = javaClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Boolean getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(Boolean isStatic) {
        this.isStatic = isStatic;
    }

    public Boolean getIsAbstract() {
        return isAbstract;
    }

    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Boolean getIsSynchronized() {
        return isSynchronized;
    }

    public void setIsSynchronized(Boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<String> getThrowsExceptions() {
        return throwsExceptions;
    }

    public void setThrowsExceptions(List<String> throwsExceptions) {
        this.throwsExceptions = throwsExceptions;
    }

    public Integer getLineCount() {
        return lineCount;
    }

    public void setLineCount(Integer lineCount) {
        this.lineCount = lineCount;
    }

    public Integer getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(Integer cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public Boolean getHasJavadoc() {
        return hasJavadoc;
    }

    public void setHasJavadoc(Boolean hasJavadoc) {
        this.hasJavadoc = hasJavadoc;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public void setJavadoc(String javadoc) {
        this.javadoc = javadoc;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public BobOutput getBobOutput() {
        return bobOutput;
    }

    public void setBobOutput(BobOutput bobOutput) {
        this.bobOutput = bobOutput;
    }

    // Business logic helpers
    public String getMethodSignature() {
        StringBuilder signature = new StringBuilder();
        
        if (visibility != null) {
            signature.append(visibility).append(" ");
        }
        if (Boolean.TRUE.equals(isStatic)) {
            signature.append("static ");
        }
        if (Boolean.TRUE.equals(isFinal)) {
            signature.append("final ");
        }
        if (returnType != null) {
            signature.append(returnType).append(" ");
        }
        signature.append(methodName).append("(");
        
        if (parameters != null && !parameters.isEmpty()) {
            signature.append(String.join(", ", parameters));
        }
        
        signature.append(")");
        
        if (throwsExceptions != null && !throwsExceptions.isEmpty()) {
            signature.append(" throws ").append(String.join(", ", throwsExceptions));
        }
        
        return signature.toString();
    }

    public boolean isComplexMethod() {
        return cyclomaticComplexity != null && cyclomaticComplexity > 10;
    }

    public boolean isLongMethod() {
        return lineCount != null && lineCount > 50;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Method)) return false;
        Method method = (Method) o;
        return methodId != null && methodId.equals(method.getMethodId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Method{" +
                "methodId=" + methodId +
                ", methodName='" + methodName + '\'' +
                ", returnType='" + returnType + '\'' +
                ", visibility='" + visibility + '\'' +
                ", lineCount=" + lineCount +
                ", cyclomaticComplexity=" + cyclomaticComplexity +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Made with Bob
