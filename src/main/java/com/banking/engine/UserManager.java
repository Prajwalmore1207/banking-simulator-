package com.banking.engine;

import com.banking.model.User;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final Logger logger = LoggerUtil.getLogger(UserManager.class);
    private DatabaseManager databaseManager;
    private Map<String, User> activeUsers;

    public UserManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.activeUsers = new HashMap<>();
        logger.info("UserManager initialized successfully");
    }

    public boolean registerUser(User user) {
        try {
            if (databaseManager.getUserByUsername(user.getUsername()) != null) {
                logger.warn("Registration failed - username already exists: {}", user.getUsername());
                return false;
            }

            boolean success = databaseManager.createUser(user);
            if (success) {
                logger.info("User registered successfully: {}", user.getUsername());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", user.getUsername(), e);
        }
        return false;
    }

    public User loginUser(String username, String passwordHash) {
        try {
            User user = databaseManager.getUserByUsername(username);
            if (user != null && user.getPasswordHash().equals(passwordHash)) {
                databaseManager.updateUserLastLogin(user.getUserId());
                activeUsers.put(username, user);
                logger.info("User logged in successfully: {}", username);
                return user;
            }
        } catch (Exception e) {
            logger.error("Error during user login: {}", username, e);
        }
        return null;
    }

    public void logoutUser(String username) {
        if (activeUsers.containsKey(username)) {
            activeUsers.remove(username);
            logger.info("User logged out: {}", username);
        }
    }

    public boolean isUserActive(String username) {
        return activeUsers.containsKey(username);
    }

    public User getActiveUser(String username) {
        return activeUsers.get(username);
    }
}