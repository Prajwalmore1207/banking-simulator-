package com.banking.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final SecureRandom random = new SecureRandom();

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Combine salt and hashed password
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing algorithm not available", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);
            byte[] salt = new byte[16];
            byte[] storedPassword = new byte[combined.length - 16];

            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, storedPassword, 0, storedPassword.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            return MessageDigest.isEqual(hashedPassword, storedPassword);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password verification failed", e);
        }
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isUpperCase(c)) hasUpper = true;
            if ("@#$%^&+=!".indexOf(c) >= 0) hasSpecial = true;
        }

        return hasDigit && hasLower && hasUpper && hasSpecial;
    }
}