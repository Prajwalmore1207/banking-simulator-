package com.banking.service;

import com.banking.model.User;
import com.banking.database.DatabaseManager;
import com.banking.util.PasswordUtil;
import com.banking.util.LoggerUtil;
import com.banking.exception.AuthenticationException;
import org.apache.logging.log4j.Logger;

public class AuthService {
    private static final Logger logger = LoggerUtil.getLogger(AuthService.class);
    private DatabaseManager databaseManager;
    private User currentUser;

    public AuthService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        logger.info("AuthService initialized successfully");
    }

    public User registerUser(String username, String password, String email,
                             String fullName, String phoneNumber) throws AuthenticationException {

        logger.info("Attempting user registration: {}", username);
        logger.debug("Registration details - Email: {}, Name: {}, Phone: {}", email, fullName, phoneNumber);

        try {
            // Validate inputs
            validateRegistrationInput(username, password, email, fullName);

            // Check if username already exists
            User existingUser = databaseManager.getUserByUsername(username);
            if (existingUser != null) {
                logger.warn("Username already exists: {}", username);
                throw new AuthenticationException("Username already exists: " + username);
            }

            // Validate password strength
            if (!PasswordUtil.isStrongPassword(password)) {
                logger.warn("Weak password for user: {}", username);
                throw new AuthenticationException(
                        "Password must be at least 8 characters long and contain digit, lowercase, uppercase, and special character");
            }

            // Hash password
            String passwordHash = PasswordUtil.hashPassword(password);
            logger.debug("Password hashed successfully for user: {}", username);

            // Create user
            User user = new User(username, passwordHash, email, fullName, phoneNumber);
            boolean success = databaseManager.createUser(user);

            if (success) {
                // Get the complete user object with user_id from database
                User registeredUser = databaseManager.getUserByUsername(username);
                logger.info("User registered successfully: {}", username);
                return registeredUser; // Return the user object
            } else {
                logger.error("Database failed to create user: {}", username);
                throw new AuthenticationException("Failed to create user account in database");
            }

        } catch (AuthenticationException e) {
            logger.warn("Registration failed for {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration for {}: {}", username, e.getMessage(), e);
            throw new AuthenticationException("Registration failed due to system error: " + e.getMessage());
        }
    }
    public User login(String username, String password) throws AuthenticationException {
        logger.info("Attempting login: {}", username);

        try {
            // Validate inputs
            if (username == null || username.trim().isEmpty()) {
                throw new AuthenticationException("Username cannot be empty");
            }
            if (password == null || password.isEmpty()) {
                throw new AuthenticationException("Password cannot be empty");
            }

            // Get user from database
            User user = databaseManager.getUserByUsername(username);
            if (user == null) {
                throw new AuthenticationException("Invalid username or password");
            }

            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password");
            }

            // Update last login
            databaseManager.updateUserLastLogin(user.getUserId());

            this.currentUser = user;
            logger.info("User logged in successfully: {}", username);

            return user;

        } catch (AuthenticationException e) {
            logger.warn("Login failed for {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", username, e);
            throw new AuthenticationException("Login failed due to system error");
        }
    }

    public void logout() {
        if (currentUser != null) {
            logger.info("User logging out: {}", currentUser.getUsername());
            currentUser = null;
        }
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void validateRegistrationInput(String username, String password, String email, String fullName)
            throws AuthenticationException {

        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            throw new AuthenticationException("Username must be at least 3 characters long");
        }

        if (password == null || password.length() < 8) {
            throw new AuthenticationException("Password must be at least 8 characters long");
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new AuthenticationException("Invalid email format");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new AuthenticationException("Full name cannot be empty");
        }
    }
}