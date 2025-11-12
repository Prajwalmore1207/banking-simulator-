package com.banking.engine;

import com.banking.model.Account;
import com.banking.exception.InvalidAccountException;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AccountManager {
    private static final Logger logger = LoggerUtil.getLogger(AccountManager.class);
    private DatabaseManager databaseManager;
    private Map<String, Account> accountCache;

    public AccountManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.accountCache = new HashMap<>();
        logger.info("AccountManager initialized successfully");
    }

    public void createAccount(Account account) throws InvalidAccountException {
        try {
            if (accountCache.containsKey(account.getAccountNumber()) ||
                    databaseManager.getAccount(account.getAccountNumber()) != null) {
                String errorMsg = "Account already exists: " + account.getAccountNumber();
                logger.warn(errorMsg);
                throw new InvalidAccountException(errorMsg);
            }

            boolean success = databaseManager.createAccount(account);
            if (success) {
                accountCache.put(account.getAccountNumber(), account);
                logger.info("Account created successfully: {}", account.getAccountNumber());
                logger.debug("Account details: {}", account);
            } else {
                throw new InvalidAccountException("Failed to create account in database");
            }

        } catch (Exception e) {
            logger.error("Failed to create account: {}", account.getAccountNumber(), e);
            if (e instanceof InvalidAccountException) {
                throw (InvalidAccountException) e;
            }
            throw new InvalidAccountException("Account creation failed: " + e.getMessage());
        }
    }

    public Account getAccount(String accountNumber) throws InvalidAccountException {
        logger.debug("Retrieving account: {}", accountNumber);

        // Check cache first
        if (accountCache.containsKey(accountNumber)) {
            return accountCache.get(accountNumber);
        }

        // Get from database
        Account account = databaseManager.getAccount(accountNumber);
        if (account == null) {
            String errorMsg = "Account not found: " + accountNumber;
            logger.warn(errorMsg);
            throw new InvalidAccountException(errorMsg);
        }

        // Add to cache
        accountCache.put(accountNumber, account);
        logger.debug("Account retrieved successfully: {}", accountNumber);
        return account;
    }

    public double getAccountBalance(String accountNumber) throws InvalidAccountException {
        Account account = getAccount(accountNumber);
        double balance = account.getBalance();
        logger.debug("Balance query for account {}: ${}", accountNumber, balance);
        return balance;
    }

    public void updateAccountBalance(String accountNumber, double newBalance) throws InvalidAccountException {
        try {
            Account account = getAccount(accountNumber);
            account.setBalance(newBalance);

            // Update database
            boolean success = databaseManager.updateAccountBalance(accountNumber, newBalance);
            if (success) {
                // Update cache
                accountCache.put(accountNumber, account);
                logger.debug("Account balance updated in system: {} -> ${}", accountNumber, newBalance);
            } else {
                throw new InvalidAccountException("Failed to update account balance in database");
            }

        } catch (InvalidAccountException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update account balance: {}", accountNumber, e);
            throw new InvalidAccountException("Balance update failed: " + e.getMessage());
        }
    }

    public List<Account> getUserAccounts(int userId) {
        List<Account> accounts = databaseManager.getUserAccounts(userId);

        // Update cache with retrieved accounts
        for (Account account : accounts) {
            accountCache.put(account.getAccountNumber(), account);
        }

        logger.debug("Retrieved {} accounts for user: {}", accounts.size(), userId);
        return accounts;
    }

    public void clearCache() {
        accountCache.clear();
        logger.debug("Account cache cleared");
    }
}