package com.backend.CodeSheriff.Service;

import com.backend.CodeSheriff.Entity.User;
import com.backend.CodeSheriff.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing User entities.
 * Handles user creation, retrieval, and synchronization with Supabase Auth.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find user by ID.
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        logger.debug("Finding user by ID: {}", userId);
        return userRepository.findById(userId);
    }

    /**
     * Find user by email.
     * @param email the email address
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Get or create user from Supabase Auth.
     * This method is called after JWT authentication to ensure the user exists in our database.
     *
     * @param supabaseId the Supabase Auth user ID as String
     * @param email the user's email
     * @return the user entity
     */
    public User getOrCreateUser(String supabaseId, String email) {
        logger.debug("Getting or creating user: {} ({})", email, supabaseId);
        
        UUID userId = UUID.fromString(supabaseId);
        
        return userRepository.findById(userId)
            .orElseGet(() -> {
                logger.info("Creating new user: {} ({})", email, userId);
                User newUser = new User(userId, email);
                newUser.setIsActive(true);
                return userRepository.save(newUser);
            });
    }

    /**
     * Get or create user from Supabase Auth (UUID overload).
     * @param userId the Supabase Auth user ID
     * @param email the user's email
     * @return the user entity
     */
    public User getOrCreateUser(UUID userId, String email) {
        logger.debug("Getting or creating user: {} ({})", email, userId);
        
        return userRepository.findById(userId)
            .orElseGet(() -> {
                logger.info("Creating new user: {} ({})", email, userId);
                User newUser = new User(userId, email);
                newUser.setIsActive(true);
                return userRepository.save(newUser);
            });
    }

    /**
     * Update user's last login timestamp.
     * @param userId the user ID
     */
    public void updateLastLogin(UUID userId) {
        logger.debug("Updating last login for user: {}", userId);
        
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
            logger.debug("Updated last login for user: {}", userId);
        });
    }

    /**
     * Update user profile information.
     * @param userId the user ID
     * @param fullName the full name
     * @param avatarUrl the avatar URL
     * @return the updated user
     */
    public Optional<User> updateProfile(UUID userId, String fullName, String avatarUrl) {
        logger.debug("Updating profile for user: {}", userId);
        
        return userRepository.findById(userId).map(user -> {
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            User updated = userRepository.save(user);
            logger.info("Updated profile for user: {}", userId);
            return updated;
        });
    }

    /**
     * Deactivate a user account.
     * @param userId the user ID
     * @return true if deactivated successfully
     */
    public boolean deactivateUser(UUID userId) {
        logger.debug("Deactivating user: {}", userId);
        
        return userRepository.findById(userId).map(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            logger.info("Deactivated user: {}", userId);
            return true;
        }).orElse(false);
    }

    /**
     * Reactivate a user account.
     * @param userId the user ID
     * @return true if reactivated successfully
     */
    public boolean reactivateUser(UUID userId) {
        logger.debug("Reactivating user: {}", userId);
        
        return userRepository.findById(userId).map(user -> {
            user.setIsActive(true);
            userRepository.save(user);
            logger.info("Reactivated user: {}", userId);
            return true;
        }).orElse(false);
    }

    /**
     * Check if a user exists.
     * @param userId the user ID
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Check if an email is already registered.
     * @param email the email address
     * @return true if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get total number of active users.
     * @return count of active users
     */
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepository.countByIsActiveTrue();
    }

    /**
     * Get user by Supabase ID (converted from String).
     * @param supabaseId the Supabase user ID as string
     * @return the user or null if not found
     */
    @Transactional(readOnly = true)
    public User getUserBySupabaseId(String supabaseId) {
        try {
            UUID userId = UUID.fromString(supabaseId);
            return userRepository.findById(userId).orElse(null);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid UUID format for supabaseId: {}", supabaseId);
            return null;
        }
    }

    /**
     * Delete a user and all associated data.
     * WARNING: This is a destructive operation. Use with caution.
     * 
     * @param userId the user ID
     * @return true if deleted successfully
     */
    public boolean deleteUser(UUID userId) {
        logger.warn("Deleting user and all associated data: {}", userId);
        
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            logger.info("Deleted user: {}", userId);
            return true;
        }
        
        return false;
    }
}

// Made with Bob
