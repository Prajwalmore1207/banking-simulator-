package com.banking.service;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.engine.AccountManager;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BalanceTracker {
    private static final Logger logger = LoggerUtil.getLogger(BalanceTracker.class);
    private AccountManager accountManager;
    private AlertService alertService;
    private DatabaseManager databaseManager;
    private ScheduledExecutorService scheduler;
    private boolean monitoringActive;

    public BalanceTracker(AccountManager accountManager, AlertService alertService, DatabaseManager databaseManager) {
        this.accountManager = accountManager;
        this.alertService = alertService;
        this.databaseManager = databaseManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.monitoringActive = false;
        logger.info("BalanceTracker initialized successfully");
    }

    public void startAutomaticMonitoring() {
        if (!monitoringActive) {
            logger.info("Starting automatic balance monitoring...");
            monitoringActive = true;

            // Schedule balance checks every 5 minutes
            scheduler.scheduleAtFixedRate(this::monitorAllActiveAccounts, 0, 5, TimeUnit.MINUTES);

            logger.info("Automatic balance monitoring started (runs every 5 minutes)");
        }
    }

    public void stopAutomaticMonitoring() {
        if (monitoringActive) {
            logger.info("Stopping automatic balance monitoring...");
            monitoringActive = false;
            scheduler.shutdown();
            logger.info("Automatic balance monitoring stopped");
        }
    }

    public void monitorAllActiveAccounts() {
        logger.info("Starting scheduled balance monitoring for all active accounts");

        try {
            // This would monitor all accounts in the system
            // For now, we'll log the monitoring cycle
            logger.info("Scheduled balance monitoring cycle completed");

        } catch (Exception e) {
            logger.error("Error during scheduled balance monitoring", e);
        }
    }

    public void monitorUserAccounts(int userId, User user) {
        logger.info("Starting balance monitoring for user: {}", userId);

        List<Account> accounts = accountManager.getUserAccounts(userId);
        int lowBalanceCount = 0;
        int criticalBalanceCount = 0;

        for (Account account : accounts) {
            double balance = account.getBalance();

            if (balance < alertService.getCriticalBalanceThreshold()) {
                criticalBalanceCount++;
                alertService.checkLowBalance(account, user);
                logger.error("Critical balance detected - Account: {}, Balance: ${}",
                        account.getAccountNumber(), balance);

            } else if (balance < alertService.getLowBalanceThreshold()) {
                lowBalanceCount++;
                alertService.checkLowBalance(account, user);
                logger.warn("Low balance detected - Account: {}, Balance: ${}",
                        account.getAccountNumber(), balance);
            }
        }

        logger.info("User balance monitoring completed. " +
                        "Critical: {}/{}, Low: {}/{}",
                criticalBalanceCount, accounts.size(),
                lowBalanceCount, accounts.size());

        // Send summary alert if any critical balances
        if (criticalBalanceCount > 0) {
            sendBalanceSummaryAlert(user, criticalBalanceCount, lowBalanceCount, accounts.size());
        }
    }

    private void sendBalanceSummaryAlert(User user, int criticalCount, int lowCount, int totalAccounts) {
        String summaryMessage = String.format(
                "BALANCE MONITORING SUMMARY\n\n" +
                        "Dear %s,\n\n" +
                        "Your account balance monitoring has detected:\n" +
                        "- Critical Balance Accounts: %d\n" +
                        "- Low Balance Accounts: %d\n" +
                        "- Total Accounts: %d\n\n" +
                        "Please review your accounts and consider making deposits to maintain minimum balances.",
                user.getFullName(), criticalCount, lowCount, totalAccounts
        );

        // FIXED: Now using public method
        alertService.sendEmailAlert(user.getEmail(), "Balance Monitoring Summary", summaryMessage);
    }

    public void generateBalanceReport(int userId) {
        List<Account> accounts = accountManager.getUserAccounts(userId);

        logger.info("=== BALANCE MONITORING REPORT for User: {} ===", userId);

        for (Account account : accounts) {
            String status;
            if (account.getBalance() < alertService.getCriticalBalanceThreshold()) {
                status = "CRITICAL";
            } else if (account.getBalance() < alertService.getLowBalanceThreshold()) {
                status = "LOW";
            } else {
                status = "OK";
            }

            logger.info("Account: {}, Balance: ${}, Status: {}, Min Balance: ${}",
                    account.getAccountNumber(), account.getBalance(), status, account.getMinBalance());
        }

        logger.info("=== END BALANCE REPORT ===");
    }

    public void checkTransactionThreshold(Account account, User user, double amount, String transactionType) {
        // High value transaction alert
        if (amount >= alertService.getHighTransactionAmount()) {
            logger.warn("High value transaction detected - Account: {}, Amount: ${}, Type: {}",
                    account.getAccountNumber(), amount, transactionType);

            alertService.sendSecurityAlert(
                    user.getUsername(),
                    "High Value " + transactionType,
                    String.format("Amount: $%.2f, Account: %s", amount, account.getAccountNumber()),
                    user.getEmail()
            );
        }

        // Suspicious activity detection
        if (isSuspiciousActivity(account, amount, transactionType)) {
            logger.warn("Suspicious activity detected - Account: {}, Amount: ${}, Type: {}",
                    account.getAccountNumber(), amount, transactionType);

            alertService.sendSuspiciousActivityAlert(
                    account, user, "Unusual Transaction",
                    String.format("%s of $%.2f", transactionType, amount)
            );
        }
    }

    private boolean isSuspiciousActivity(Account account, double amount, String transactionType) {
        // Simple suspicious activity detection rules
        double balance = account.getBalance();

        // Large withdrawal relative to balance
        if (transactionType.equals("WITHDRAWAL") && amount > balance * 0.8) {
            return true;
        }

        // Multiple rapid transactions could be detected here
        // (would need transaction history analysis)

        return false;
    }

    public void checkNewAccountBalance(Account account, User user) {
        // Check if initial deposit meets minimum requirements
        if (account.getBalance() < account.getMinBalance()) {
            logger.warn("New account created with low initial balance - Account: {}, Balance: ${}",
                    account.getAccountNumber(), account.getBalance());

            String alertMessage = String.format(
                    "NEW ACCOUNT WITH LOW BALANCE\n\n" +
                            "Account: %s\n" +
                            "Current Balance: $%.2f\n" +
                            "Minimum Required: $%.2f\n\n" +
                            "Please deposit additional funds to meet the minimum balance requirement.",
                    account.getAccountNumber(), account.getBalance(), account.getMinBalance()
            );

            // FIXED: Now using public method
            alertService.sendEmailAlert(user.getEmail(), "Low Initial Balance", alertMessage);
        }
    }
}