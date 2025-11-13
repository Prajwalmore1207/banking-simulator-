package com.banking.service;

import com.banking.model.User;
import com.banking.database.DatabaseManager;
import com.banking.util.PasswordUtil;
import com.banking.util.LoggerUtil;
import com.banking.exception.AuthenticationException;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class AuthService {
    private static final Logger logger = LoggerUtil.getLogger(AuthService.class);
    private DatabaseManager databaseManager;
    private User currentUser;
    private Scanner scanner;

    public AuthService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.scanner = new Scanner(System.in);
        logger.info("AuthService initialized successfully");
    }

    // Method for interactive registration
    public User registerUserInteractive() throws AuthenticationException {
        System.out.println("\n=== USER REGISTRATION ===");

        try {
            // Get user details interactively
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Enter full name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Enter phone number: ");
            String phoneNumber = scanner.nextLine().trim();

            // Get password without masking
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            System.out.print("Confirm password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!password.equals(confirmPassword)) {
                throw new AuthenticationException("Passwords do not match");
            }

            // Call the existing registerUser method
            return registerUser(username, password, email, fullName, phoneNumber);

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }
            throw new AuthenticationException("Registration failed: " + e.getMessage());
        }
    }

    // Your existing registerUser method (unchanged)
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

    // Method for interactive login
    public User loginInteractive() throws AuthenticationException {
        System.out.println("\n=== USER LOGIN ===");

        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Call the existing login method
            return login(username, password);

        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    // Your existing login method (unchanged)
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

    // Method for interactive password change
    public boolean changePasswordInteractive() throws AuthenticationException {
        if (!isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        System.out.println("\n=== CHANGE PASSWORD ===");
        System.out.println("User: " + currentUser.getUsername());

        try {
            // Get current password without masking
            System.out.print("Enter current password: ");
            String currentPassword = scanner.nextLine();

            // Verify current password
            if (!PasswordUtil.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
                System.out.println("✗ Error: Current password is incorrect!");
                return false;
            }

            // Get new password without masking
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();

            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("✗ Error: New passwords do not match!");
                return false;
            }

            // Validate password strength
            if (!PasswordUtil.isStrongPassword(newPassword)) {
                System.out.println("✗ Error: New password does not meet strength requirements!");
                return false;
            }

            // Hash new password
            String newHashedPassword = PasswordUtil.hashPassword(newPassword);

            // Update password in database
            boolean success = databaseManager.updatePassword(currentUser.getUserId(), newHashedPassword);

            if (success) {
                // Update current user object
                currentUser.setPasswordHash(newHashedPassword);
                System.out.println("✓ Password changed successfully!");
                logger.info("Password changed for user: {}", currentUser.getUsername());
                return true;
            } else {
                System.out.println("✗ Failed to change password!");
                logger.error("Password change failed for user: {}", currentUser.getUsername());
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error changing password: " + e.getMessage());
            logger.error("Password change error for user {}: {}", currentUser.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    // Method to reset password (for forgotten passwords)
    public boolean resetPasswordInteractive() throws AuthenticationException {
        System.out.println("\n=== PASSWORD RESET ===");

        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();

            // Verify user exists and email matches
            User user = databaseManager.getUserByUsername(username);
            if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
                System.out.println("✗ Error: Invalid username or email!");
                throw new AuthenticationException("Invalid username or email");
            }

            // Get new password without masking
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine();

            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("✗ Error: Passwords do not match!");
                throw new AuthenticationException("Passwords do not match");
            }

            // Validate password strength
            if (!PasswordUtil.isStrongPassword(newPassword)) {
                System.out.println("✗ Error: Password does not meet strength requirements!");
                throw new AuthenticationException("Password does not meet strength requirements");
            }

            // Hash new password
            String newHashedPassword = PasswordUtil.hashPassword(newPassword);

            // Update password in database
            boolean success = databaseManager.updatePassword(user.getUserId(), newHashedPassword);

            if (success) {
                System.out.println("✓ Password reset successfully!");
                logger.info("Password reset for user: {}", username);
                return true;
            } else {
                System.out.println("✗ Failed to reset password!");
                logger.error("Password reset failed for user: {}", username);
                return false;
            }

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Password reset failed: " + e.getMessage());
        }
    }

    // Your existing methods (unchanged)
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

    // Close scanner when done
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}