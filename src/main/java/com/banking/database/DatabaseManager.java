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
}