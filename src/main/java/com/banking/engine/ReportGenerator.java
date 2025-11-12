package com.banking.service;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.engine.AccountManager;
import com.banking.engine.TransactionProcessor;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {
    private static final Logger logger = LoggerUtil.getLogger(ReportGenerator.class);
    private AccountManager accountManager;
    private TransactionProcessor transactionProcessor;
    private DatabaseManager databaseManager;

    public ReportGenerator(AccountManager accountManager, TransactionProcessor transactionProcessor, DatabaseManager databaseManager) {
        this.accountManager = accountManager;
        this.transactionProcessor = transactionProcessor;
        this.databaseManager = databaseManager;
        logger.info("ReportGenerator initialized successfully");
    }

    public void generateAccountSummaryReport(String filename) {
        logger.info("Generating account summary report: {}", filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== ACCOUNT SUMMARY REPORT ===");
            writer.println("Generated at: " + java.time.LocalDateTime.now());
            writer.println("=================================");
            writer.println();

            writer.println("Note: Full account reporting requires user authentication.");
            writer.println("Please use the application menu to view your accounts.");

            logger.info("Account summary report generated successfully: {}", filename);

        } catch (IOException e) {
            logger.error("Failed to generate account summary report: {}", filename, e);
        }
    }

    public void generateUserAccountReport(String filename, int userId) {
        logger.info("Generating user account report: {} for user: {}", filename, userId);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<Account> accounts = accountManager.getUserAccounts(userId);

            writer.println("=== USER ACCOUNT SUMMARY REPORT ===");
            writer.println("Generated at: " + java.time.LocalDateTime.now());
            writer.println("Total Accounts: " + accounts.size());
            writer.println("=====================================");
            writer.println();

            double totalBalance = 0;
            for (Account account : accounts) {
                writer.printf("Account: %s%n", account.getAccountNumber());
                writer.printf("Holder: %s%n", account.getAccountHolderName());
                writer.printf("Balance: $%.2f%n", account.getBalance());
                writer.printf("Type: %s%n", account.getAccountType());
                writer.printf("Created: %s%n",
                        account.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                writer.println("---------------------------");

                totalBalance += account.getBalance();
            }

            writer.println();
            writer.printf("TOTAL BALANCE ACROSS ALL ACCOUNTS: $%.2f%n", totalBalance);

            logger.info("User account report generated successfully: {}", filename);

        } catch (IOException e) {
            logger.error("Failed to generate user account report: {}", filename, e);
        } catch (Exception e) {
            logger.error("Error generating user account report for user {}: {}", userId, e.getMessage());
        }
    }

    public void generateTransactionReport(String filename) {
        logger.info("Generating transaction report: {}", filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== TRANSACTION HISTORY REPORT ===");
            writer.println("Generated at: " + java.time.LocalDateTime.now());
            writer.println("===================================");
            writer.println();

            writer.println("Note: Full transaction reporting requires account selection.");
            writer.println("Please use the application menu to view transaction history.");

            logger.info("Transaction report generated successfully: {}", filename);

        } catch (IOException e) {
            logger.error("Failed to generate transaction report: {}", filename, e);
        }
    }

    public void generateAccountTransactionReport(String filename, String accountNumber) {
        logger.info("Generating account transaction report: {} for account: {}", filename, accountNumber);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<Transaction> transactions = transactionProcessor.getAccountTransactions(accountNumber);

            writer.println("=== ACCOUNT TRANSACTION HISTORY REPORT ===");
            writer.println("Generated at: " + java.time.LocalDateTime.now());
            writer.println("Account: " + accountNumber);
            writer.println("Total Transactions: " + transactions.size());
            writer.println("===========================================");
            writer.println();

            for (Transaction transaction : transactions) {
                writer.printf("Transaction ID: %s%n", transaction.getTransactionId());
                writer.printf("Type: %s%n", transaction.getType());
                writer.printf("From Account: %s%n",
                        transaction.getFromAccount() != null ? transaction.getFromAccount() : "N/A");
                writer.printf("To Account: %s%n",
                        transaction.getToAccount() != null ? transaction.getToAccount() : "N/A");
                writer.printf("Amount: $%.2f%n", transaction.getAmount());
                writer.printf("Time: %s%n",
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.printf("Status: %s%n", transaction.isStatus() ? "SUCCESS" : "FAILED");
                writer.printf("Description: %s%n", transaction.getDescription());
                writer.println("-----------------------------------");
            }

            logger.info("Account transaction report generated successfully: {}", filename);

        } catch (IOException e) {
            logger.error("Failed to generate account transaction report: {}", filename, e);
        } catch (Exception e) {
            logger.error("Error generating transaction report for account {}: {}", accountNumber, e.getMessage());
        }
    }
}