package com.banking.engine;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.exception.*;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TransactionProcessor {
    private static final Logger logger = LoggerUtil.getLogger(TransactionProcessor.class);
    private AccountManager accountManager;
    private DatabaseManager databaseManager;
    private List<Transaction> transactionHistory;
    private int currentUserId; // Track current user for security

    public TransactionProcessor(AccountManager accountManager, DatabaseManager databaseManager, int currentUserId) {
        this.accountManager = accountManager;
        this.databaseManager = databaseManager;
        this.currentUserId = currentUserId;
        this.transactionHistory = new ArrayList<>();
        logger.info("TransactionProcessor initialized successfully for user: {}", currentUserId);
    }

    // Update all methods to validate account ownership

    public Transaction deposit(String accountNumber, double amount)
            throws InvalidAccountException, InvalidTransactionException {

        logger.info("Processing deposit: Account={}, Amount=${}", accountNumber, amount);

        try {
            validateAmount(amount);
            Account account = accountManager.getAccount(accountNumber);

            // SECURITY: Verify account belongs to current user
            validateAccountOwnership(account, accountNumber);

            double newBalance = account.getBalance() + amount;
            accountManager.updateAccountBalance(accountNumber, newBalance);

            Transaction transaction = new Transaction(
                    generateTransactionId(), accountNumber, null, amount,
                    TransactionType.DEPOSIT, "Cash deposit"
            );

            databaseManager.logTransaction(transaction);
            transactionHistory.add(transaction);

            logger.info("Deposit successful: Transaction={}, New Balance=${}",
                    transaction.getTransactionId(), newBalance);

            return transaction;

        } catch (InvalidAccountException | InvalidTransactionException e) {
            logger.error("Deposit failed for account {}: {}", accountNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during deposit for account {}: {}", accountNumber, e.getMessage());
            throw new InvalidTransactionException("Deposit failed: " + e.getMessage());
        }
    }

    public Transaction withdraw(String accountNumber, double amount)
            throws InvalidAccountException, InsufficientFundsException, InvalidTransactionException {

        logger.info("Processing withdrawal: Account={}, Amount=${}", accountNumber, amount);

        try {
            validateAmount(amount);
            Account account = accountManager.getAccount(accountNumber);

            // SECURITY: Verify account belongs to current user
            validateAccountOwnership(account, accountNumber);

            if (account.getBalance() - amount < account.getMinBalance()) {
                throw new InsufficientFundsException(accountNumber, account.getBalance(), amount);
            }

            double newBalance = account.getBalance() - amount;
            accountManager.updateAccountBalance(accountNumber, newBalance);

            Transaction transaction = new Transaction(
                    generateTransactionId(), accountNumber, null, amount,
                    TransactionType.WITHDRAWAL, "Cash withdrawal"
            );

            databaseManager.logTransaction(transaction);
            transactionHistory.add(transaction);

            logger.info("Withdrawal successful: Transaction={}, New Balance=${}",
                    transaction.getTransactionId(), newBalance);

            return transaction;

        } catch (InvalidAccountException | InsufficientFundsException | InvalidTransactionException e) {
            logger.error("Withdrawal failed for account {}: {}", accountNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during withdrawal for account {}: {}", accountNumber, e.getMessage());
            throw new InvalidTransactionException("Withdrawal failed: " + e.getMessage());
        }
    }

    public Transaction transfer(String fromAccount, String toAccount, double amount)
            throws InvalidAccountException, InsufficientFundsException, InvalidTransactionException {

        logger.info("Processing transfer: From={}, To={}, Amount=${}", fromAccount, toAccount, amount);

        try {
            validateAmount(amount);

            // Check if both accounts exist
            Account source = accountManager.getAccount(fromAccount);
            Account destination = accountManager.getAccount(toAccount);

            // SECURITY: Verify source account belongs to current user
            validateAccountOwnership(source, fromAccount);

            // Check sufficient funds
            if (source.getBalance() - amount < source.getMinBalance()) {
                throw new InsufficientFundsException(fromAccount, source.getBalance(), amount);
            }

            // Perform transfer
            double sourceNewBalance = source.getBalance() - amount;
            double destNewBalance = destination.getBalance() + amount;

            accountManager.updateAccountBalance(fromAccount, sourceNewBalance);
            accountManager.updateAccountBalance(toAccount, destNewBalance);

            Transaction transaction = new Transaction(
                    generateTransactionId(), fromAccount, toAccount, amount,
                    TransactionType.TRANSFER, "Fund transfer"
            );

            databaseManager.logTransaction(transaction);
            transactionHistory.add(transaction);

            logger.info("Transfer successful: Transaction={}, From Balance=${}, To Balance=${}",
                    transaction.getTransactionId(), sourceNewBalance, destNewBalance);

            return transaction;

        } catch (InvalidAccountException | InsufficientFundsException | InvalidTransactionException e) {
            logger.error("Transfer failed from {} to {}: {}", fromAccount, toAccount, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during transfer from {} to {}: {}", fromAccount, toAccount, e.getMessage());
            throw new InvalidTransactionException("Transfer failed: " + e.getMessage());
        }
    }

    // SECURITY: Add ownership validation method
    private void validateAccountOwnership(Account account, String accountNumber)
            throws InvalidAccountException {
        if (account.getUserId() != currentUserId) {
            logger.warn("Security violation: User {} attempted to access account {} owned by user {}",
                    currentUserId, accountNumber, account.getUserId());
            throw new InvalidAccountException("Access denied: You can only access your own accounts");
        }
    }

    private void validateAmount(double amount) throws InvalidTransactionException {
        if (amount <= 0) {
            String errorMsg = "Invalid amount: " + amount;
            logger.warn(errorMsg);
            throw new InvalidTransactionException(errorMsg);
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public List<Transaction> getAccountTransactions(String accountNumber) throws InvalidAccountException {
        // SECURITY: Validate ownership before returning transactions
        Account account = accountManager.getAccount(accountNumber);
        validateAccountOwnership(account, accountNumber);
        return databaseManager.getAccountTransactions(accountNumber);
    }

    // Update current user when user logs in/out
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        logger.debug("TransactionProcessor updated for user: {}", userId);
    }
}