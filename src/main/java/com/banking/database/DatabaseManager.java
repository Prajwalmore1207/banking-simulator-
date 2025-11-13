package com.banking.database;

import com.banking.model.User;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final Logger logger = LoggerUtil.getLogger(DatabaseManager.class);

    // User Management Methods
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, phone_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getPhoneNumber());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("User created successfully: {}", user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
        }
        return false;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone_number")
                );
                user.setUserId(rs.getInt("user_id"));
                user.setLastLogin(rs.getTimestamp("last_login") != null ?
                        rs.getTimestamp("last_login").toLocalDateTime() : null);
                user.setActive(rs.getBoolean("is_active"));

                logger.debug("User retrieved: {}", username);
                return user;
            }
        } catch (SQLException e) {
            logger.error("Failed to get user: {}", username, e);
        }
        return null;
    }

    public boolean updateUserLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                logger.debug("User last login updated: {}", userId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update user last login: {}", userId, e);
        }
        return false;
    }

    // Account Management Methods
    public boolean createAccount(Account account) {
        String sql = "INSERT INTO accounts (account_number, user_id, account_holder_name, balance, account_type) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountNumber());
            pstmt.setInt(2, account.getUserId());
            pstmt.setString(3, account.getAccountHolderName());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setString(5, account.getAccountType());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Account created successfully: {}", account.getAccountNumber());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to create account: {}", account.getAccountNumber(), e);
        }
        return false;
    }

    public Account getAccount(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Account account = new Account(
                        rs.getString("account_number"),
                        rs.getInt("user_id"),
                        rs.getString("account_holder_name"),
                        rs.getDouble("balance"),
                        rs.getString("account_type")
                );
                account.setActive(rs.getBoolean("is_active"));

                logger.debug("Account retrieved: {}", accountNumber);
                return account;
            }
        } catch (SQLException e) {
            logger.error("Failed to get account: {}", accountNumber, e);
        }
        return null;
    }

    public boolean updateAccountBalance(String accountNumber, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountNumber);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.debug("Account balance updated: {} -> ${}", accountNumber, newBalance);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to update account balance: {}", accountNumber, e);
        }
        return false;
    }

    public List<Account> getUserAccounts(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Account account = new Account(
                        rs.getString("account_number"),
                        rs.getInt("user_id"),
                        rs.getString("account_holder_name"),
                        rs.getDouble("balance"),
                        rs.getString("account_type")
                );
                account.setActive(rs.getBoolean("is_active"));
                accounts.add(account);
            }

            logger.debug("Retrieved {} accounts for user: {}", accounts.size(), userId);
        } catch (SQLException e) {
            logger.error("Failed to get user accounts: {}", userId, e);
        }
        return accounts;
    }

    // Transaction Management Methods
    public boolean logTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, from_account, to_account, amount, transaction_type, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getFromAccount());
            pstmt.setString(3, transaction.getToAccount());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setString(5, transaction.getType().toString());
            pstmt.setString(6, transaction.getDescription());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.debug("Transaction logged: {}", transaction.getTransactionId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Failed to log transaction: {}", transaction.getTransactionId(), e);
        }
        return false;
    }

    public List<Transaction> getAccountTransactions(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account = ? OR to_account = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            pstmt.setString(2, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getString("transaction_id"),
                        rs.getString("from_account"),
                        rs.getString("to_account"),
                        rs.getDouble("amount"),
                        TransactionType.valueOf(rs.getString("transaction_type")),
                        rs.getString("description")
                );
                transaction.setStatus(rs.getBoolean("status"));
                transactions.add(transaction);
            }

            logger.debug("Retrieved {} transactions for account: {}", transactions.size(), accountNumber);
        } catch (SQLException e) {
            logger.error("Failed to get account transactions: {}", accountNumber, e);
        }
        return transactions;
    }

    // ===== PASSWORD MANAGEMENT METHODS =====

    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Password updated successfully for user ID: {}", userId);
            } else {
                logger.warn("No user found with ID: {} for password update", userId);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Error updating password for user ID {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Update password by username (for password reset)
     */
    public boolean updatePasswordByUsername(String username, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;

            if (success) {
                logger.info("Password updated successfully for username: {}", username);
            } else {
                logger.warn("No user found with username: {} for password update", username);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Error updating password for username {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Get password hash for a user
     */
    public String getPasswordHash(int userId) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password_hash");
            }

        } catch (SQLException e) {
            logger.error("Error getting password hash for user ID {}: {}", userId, e.getMessage());
        }

        return null;
    }

    /**
     * Get password hash by username
     */
    public String getPasswordHashByUsername(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password_hash");
            }

        } catch (SQLException e) {
            logger.error("Error getting password hash for username {}: {}", username, e.getMessage());
        }

        return null;
    }

    /**
     * Check if username exists
     */
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.error("Error checking if username exists {}: {}", username, e.getMessage());
        }

        return false;
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.error("Error checking if email exists {}: {}", email, e.getMessage());
        }

        return false;
    }

    /**
     * Verify user credentials for password reset
     */
    public boolean verifyUserCredentials(String username, String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.error("Error verifying user credentials for {}: {}", username, e.getMessage());
        }

        return false;
    }

    /**
     * Get user ID by username
     */
    public Integer getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (SQLException e) {
            logger.error("Error getting user ID for username {}: {}", username, e.getMessage());
        }

        return null;
    }

    /**
     * Get username by user ID
     */
    public String getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }

        } catch (SQLException e) {
            logger.error("Error getting username for user ID {}: {}", userId, e.getMessage());
        }

        return null;
    }

    /**
     * Get user by user ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone_number")
                );
                user.setUserId(rs.getInt("user_id"));
                user.setLastLogin(rs.getTimestamp("last_login") != null ?
                        rs.getTimestamp("last_login").toLocalDateTime() : null);
                user.setActive(rs.getBoolean("is_active"));

                logger.debug("User retrieved by ID: {}", userId);
                return user;
            }
        } catch (SQLException e) {
            logger.error("Failed to get user by ID: {}", userId, e);
        }
        return null;
    }

    /**
     * Save user (alternative to createUser for updates)
     * REMOVED the problematic saveUser method to avoid circular reference
     */
    // Remove the saveUser method that was causing the circular reference
}