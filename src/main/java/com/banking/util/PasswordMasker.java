package com.banking.util;

import java.io.Console;
import java.io.IOException;

public class PasswordMasker {

    /**
     * Reads password with immediate asterisk conversion
     */
    public static String readPassword() {
        return readPassword("Enter password: ");
    }

    public static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            // Use Console for best password masking
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        } else {
            // Enhanced masking for IDEs with immediate asterisk conversion
            return readPasswordWithImmediateMasking(prompt);
        }
    }

    /**
     * Password reading with immediate asterisk conversion (no character flash)
     */
    private static String readPasswordWithImmediateMasking(String prompt) {
        System.out.print(prompt);

        StringBuilder password = new StringBuilder();
        try {
            // Disable line buffering and echo immediately
            disableConsoleEcho();

            int currentChar;
            while (true) {
                currentChar = System.in.read();

                if (currentChar == '\n' || currentChar == '\r' || currentChar == -1) {
                    break; // Enter pressed
                } else if (currentChar == 8 || currentChar == 127) {
                    // Backspace handling
                    if (password.length() > 0) {
                        password.deleteCharAt(password.length() - 1);
                        // Clear the asterisk: backspace, space, backspace
                        System.out.print("\b \b");
                    }
                } else if (currentChar == 3) { // Ctrl+C
                    throw new RuntimeException("Operation cancelled");
                } else if (currentChar >= 32 && currentChar <= 126) {
                    // Printable character - show asterisk IMMEDIATELY
                    password.append((char) currentChar);
                    System.out.print('*'); // Show asterisk immediately
                }
                // Force flush to ensure immediate display
                System.out.flush();
            }

        } catch (IOException e) {
            System.out.println("\nError reading password");
            return "";
        } catch (RuntimeException e) {
            System.out.println("\n" + e.getMessage());
            return "";
        } finally {
            // Restore console settings
            enableConsoleEcho();
            System.out.println(); // Move to next line
        }

        return password.toString();
    }

    /**
     * Disable console echo to prevent character flashing
     */
    private static void disableConsoleEcho() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "stty -echo").start().waitFor();
            } else {
                // Unix/Linux/Mac
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty -echo </dev/tty"}).waitFor();
            }
        } catch (Exception e) {
            // If this fails, we'll rely on immediate asterisk printing
        }
    }

    /**
     * Enable console echo
     */
    private static void enableConsoleEcho() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "stty echo").start().waitFor();
            } else {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty echo </dev/tty"}).waitFor();
            }
        } catch (Exception e) {
            // Ignore restoration errors
        }
    }

    /**
     * Reads password with confirmation
     */
    public static String readPasswordWithConfirmation() {
        while (true) {
            String password = readPassword("Enter password: ");

            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.\n");
                continue;
            }

            if (!isValidPassword(password)) {
                System.out.println("\nPassword must be at least 8 characters with uppercase, lowercase, digit, and special character (@#$%^&+=!)");
                System.out.println("Please try again.\n");
                continue;
            }

            String confirmPassword = readPassword("Confirm password: ");

            if (password.equals(confirmPassword)) {
                System.out.println("✓ Password accepted!");
                return password;
            } else {
                System.out.println("✗ Passwords do not match! Please try again.\n");
            }
        }
    }

    /**
     * Simple password reading for login
     */
    public static String readPasswordForLogin() {
        return readPassword("Enter password: ");
    }

    /**
     * Validates password strength
     */
    private static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasSpecial = password.matches(".*[@#$%^&+=!].*");

        return hasDigit && hasLower && hasUpper && hasSpecial;
    }

    /**
     * Regular input reading
     */
    public static String readInput(String prompt) {
        System.out.print(prompt);
        try {
            StringBuilder input = new StringBuilder();
            int currentChar;
            while ((currentChar = System.in.read()) != '\n' && currentChar != '\r' && currentChar != -1) {
                if (currentChar >= 32 && currentChar <= 126) {
                    input.append((char) currentChar);
                    // Echo the character for regular input
                    System.out.print((char) currentChar);
                }
            }
            System.out.println(); // New line after input
            return input.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }
}