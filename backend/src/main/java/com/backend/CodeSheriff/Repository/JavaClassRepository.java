package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.Analysis;
import com.backend.CodeSheriff.Entity.JavaClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for JavaClass entity.
 * Provides CRUD operations and custom queries for Java class management.
 */
@Repository
public interface JavaClassRepository extends JpaRepository<JavaClass, UUID> {

    /**
     * Find all classes for a specific analysis.
     * @param analysis the analysis
     * @return list of Java classes
     */
    List<JavaClass> findByAnalysis(Analysis analysis);

    /**
     * Find all classes for an analysis ID.
     * @param analysisId the analysis ID
     * @return list of Java classes
     */
    List<JavaClass> findByAnalysisAnalysisId(UUID analysisId);

    /**
     * Find class by name within an analysis.
     * @param analysisId the analysis ID
     * @param className the class name
     * @return Optional containing the class if found
     */
    Optional<JavaClass> findByAnalysisAnalysisIdAndClassName(UUID analysisId, String className);

    /**
     * Find classes by package name.
     * @param packageName the package name
     * @return list of Java classes
     */
    List<JavaClass> findByPackageName(String packageName);

    /**
     * Find all interfaces.
     * @return list of interface classes
     */
    List<JavaClass> findByIsInterfaceTrue();

    /**
     * Find all abstract classes.
     * @return list of abstract classes
     */
    List<JavaClass> findByIsAbstractTrue();

    /**
     * Find all enums.
     * @return list of enum classes
     */
    List<JavaClass> findByIsEnumTrue();

    /**
     * Find classes with high method counts.
     * @param minMethods minimum number of methods
     * @return list of Java classes
     */
    @Query("SELECT jc FROM JavaClass jc WHERE jc.methodCount >= :minMethods ORDER BY jc.methodCount DESC")
    List<JavaClass> findClassesWithHighMethodCount(@Param("minMethods") int minMethods);

    /**
     * Find classes by annotation.
     * @param annotation the annotation name
     * @return list of Java classes
     */
    @Query("SELECT jc FROM JavaClass jc JOIN jc.annotations a WHERE a = :annotation")
    List<JavaClass> findByAnnotation(@Param("annotation") String annotation);

    /**
     * Count classes in an analysis.
     * @param analysisId the analysis ID
     * @return count of classes
     */
    long countByAnalysisAnalysisId(UUID analysisId);

    /**
     * Find classes that extend a specific class.
     * @param extendsClass the parent class name
     * @return list of Java classes
     */
    List<JavaClass> findByExtendsClass(String extendsClass);

    /**
     * Find classes that implement a specific interface.
     * @param interfaceName the interface name
     * @return list of Java classes
     */
    @Query("SELECT jc FROM JavaClass jc JOIN jc.implementsInterfaces i WHERE i = :interfaceName")
    List<JavaClass> findByImplementsInterface(@Param("interfaceName") String interfaceName);

    /**
     * Search classes by name pattern.
     * @param pattern the search pattern
     * @return list of Java classes
     */
    List<JavaClass> findByClassNameContainingIgnoreCase(String pattern);
}

// Made with Bob
