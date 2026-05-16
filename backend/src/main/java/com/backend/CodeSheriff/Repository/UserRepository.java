package com.backend.CodeSheriff.Repository;

import com.backend.CodeSheriff.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address.
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     * @param email the email address
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users.
     * @return list of active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users who logged in after a specific date.
     * @param date the cutoff date
     * @return list of users
     */
    List<User> findByLastLoginAtAfter(Instant date);

    /**
     * Find users created within a date range.
     * @param startDate start of the range
     * @param endDate end of the range
     * @return list of users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Count active users.
     * @return number of active users
     */
    long countByIsActiveTrue();

    /**
     * Find users with analyses.
     * @return list of users who have created analyses
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.analyses a")
    List<User> findUsersWithAnalyses();
}

// Made with Bob
