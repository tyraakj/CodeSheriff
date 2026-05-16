package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.JavaClass;
import com.backend.CodeSheriff.Entity.Method;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Method entity.
 * Provides CRUD operations and custom queries for method management.
 */
@Repository
public interface MethodRepository extends JpaRepository<Method, UUID> {

    /**
     * Find all methods for a specific Java class.
     * @param javaClass the Java class
     * @return list of methods
     */
    List<Method> findByJavaClass(JavaClass javaClass);

    /**
     * Find all methods for a class ID.
     * @param classId the class ID
     * @return list of methods
     */
    List<Method> findByJavaClassClassId(UUID classId);

    /**
     * Find method by name within a class.
     * @param classId the class ID
     * @param methodName the method name
     * @return Optional containing the method if found
     */
    Optional<Method> findByJavaClassClassIdAndMethodName(UUID classId, String methodName);

    /**
     * Find methods by visibility.
     * @param visibility the visibility (public, private, protected, package-private)
     * @return list of methods
     */
    List<Method> findByVisibility(String visibility);

    /**
     * Find all static methods.
     * @return list of static methods
     */
    List<Method> findByIsStaticTrue();

    /**
     * Find all abstract methods.
     * @return list of abstract methods
     */
    List<Method> findByIsAbstractTrue();

    /**
     * Find methods with Javadoc.
     * @return list of methods with Javadoc
     */
    List<Method> findByHasJavadocTrue();

    /**
     * Find methods without Javadoc.
     * @return list of methods without Javadoc
     */
    List<Method> findByHasJavadocFalse();

    /**
     * Find complex methods (high cyclomatic complexity).
     * @param minComplexity minimum complexity threshold
     * @return list of complex methods
     */
    @Query("SELECT m FROM Method m WHERE m.cyclomaticComplexity >= :minComplexity ORDER BY m.cyclomaticComplexity DESC")
    List<Method> findComplexMethods(@Param("minComplexity") int minComplexity);

    /**
     * Find long methods (high line count).
     * @param minLines minimum line count
     * @return list of long methods
     */
    @Query("SELECT m FROM Method m WHERE m.lineCount >= :minLines ORDER BY m.lineCount DESC")
    List<Method> findLongMethods(@Param("minLines") int minLines);

    /**
     * Find methods by annotation.
     * @param annotation the annotation name
     * @return list of methods
     */
    @Query("SELECT m FROM Method m JOIN m.annotations a WHERE a = :annotation")
    List<Method> findByAnnotation(@Param("annotation") String annotation);

    /**
     * Find methods that throw specific exceptions.
     * @param exceptionType the exception type
     * @return list of methods
     */
    @Query("SELECT m FROM Method m JOIN m.throwsExceptions e WHERE e = :exceptionType")
    List<Method> findByThrowsException(@Param("exceptionType") String exceptionType);

    /**
     * Find methods with Bob analysis.
     * @return list of methods that have been analyzed by Bob
     */
    @Query("SELECT m FROM Method m WHERE m.bobOutput IS NOT NULL")
    List<Method> findMethodsWithBobAnalysis();

    /**
     * Find methods without Bob analysis.
     * @return list of methods that haven't been analyzed by Bob
     */
    @Query("SELECT m FROM Method m WHERE m.bobOutput IS NULL")
    List<Method> findMethodsWithoutBobAnalysis();

    /**
     * Count methods in a class.
     * @param classId the class ID
     * @return count of methods
     */
    long countByJavaClassClassId(UUID classId);

    /**
     * Find methods by return type.
     * @param returnType the return type
     * @return list of methods
     */
    List<Method> findByReturnType(String returnType);

    /**
     * Search methods by name pattern.
     * @param pattern the search pattern
     * @return list of methods
     */
    List<Method> findByMethodNameContainingIgnoreCase(String pattern);

    /**
     * Find all methods in an analysis.
     * @param analysisId the analysis ID
     * @return list of methods
     */
    @Query("SELECT m FROM Method m WHERE m.javaClass.analysis.analysisId = :analysisId")
    List<Method> findByAnalysisId(@Param("analysisId") UUID analysisId);
}

// Made with Bob
