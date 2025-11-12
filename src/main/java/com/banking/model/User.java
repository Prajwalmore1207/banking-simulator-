package com.banking.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    private boolean isActive;

    public User(String username, String passwordHash, String email, String fullName, String phoneNumber) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.createdDate = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("User[ID: %d, Username: %s, Name: %s]", userId, username, fullName);
    }
}