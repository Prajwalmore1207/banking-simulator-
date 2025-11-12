package com.banking.service;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.database.DatabaseManager;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

public class AlertService {
    private static final Logger logger = LoggerUtil.getLogger(AlertService.class);
    private static final double LOW_BALANCE_THRESHOLD = 500.0;
    private static final double HIGH_TRANSACTION_AMOUNT = 5000.0;
    private static final double CRITICAL_BALANCE_THRESHOLD = 100.0;

    private DatabaseManager databaseManager;
    private EmailService emailService;

    public AlertService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.emailService = new EmailService();
        logger.info("AlertService initialized successfully");
    }

    public void checkLowBalance(Account account, User user) {
        double balance = account.getBalance();

        if (balance < CRITICAL_BALANCE_THRESHOLD) {
            sendCriticalBalanceAlert(account, user, balance);
        } else if (balance < LOW_BALANCE_THRESHOLD) {
            sendLowBalanceAlert(account, user, balance);
        }
    }

    private void sendLowBalanceAlert(Account account, User user, double balance) {
        String alertMessage = String.format(
                "LOW BALANCE ALERT\n\n" +
                        "Account: %s\n" +
                        "Current Balance: $%.2f\n" +
                        "Low Balance Threshold: $%.2f\n" +
                        "Minimum Required Balance: $%.2f\n\n" +
                        "Please consider depositing funds to avoid account maintenance fees.",
                account.getAccountNumber(), balance, LOW_BALANCE_THRESHOLD, account.getMinBalance()
        );

        logger.warn("Low balance alert - Account: {}, Balance: ${}",
                account.getAccountNumber(), balance);

        sendEmailAlert(user.getEmail(), "Low Balance Warning", alertMessage);
    }

    private void sendCriticalBalanceAlert(Account account, User user, double balance) {
        String alertMessage = String.format(
                "CRITICAL BALANCE ALERT\n\n" +
                        "Account: %s\n" +
                        "Current Balance: $%.2f\n" +
                        "Critical Balance Threshold: $%.2f\n" +
                        "Minimum Required Balance: $%.2f\n\n" +
                        "URGENT: Your account balance is critically low. " +
                        "Please deposit funds immediately to avoid account suspension.",
                account.getAccountNumber(), balance, CRITICAL_BALANCE_THRESHOLD, account.getMinBalance()
        );

        logger.error("Critical balance alert - Account: {}, Balance: ${}",
                account.getAccountNumber(), balance);

        sendEmailAlert(user.getEmail(), "CRITICAL: Low Balance Alert", alertMessage);
    }

    public void sendTransactionAlert(Account account, User user, String transactionType, double amount) {
        String alertMessage = String.format(
                "TRANSACTION ALERT\n\n" +
                        "Type: %s\n" +
                        "Amount: $%.2f\n" +
                        "Account: %s\n" +
                        "Current Balance: $%.2f\n" +
                        "Time: %s",
                transactionType, amount, account.getAccountNumber(),
                account.getBalance(), java.time.LocalDateTime.now()
        );

        logger.info("Transaction alert - Account: {}, Type: {}, Amount: ${}",
                account.getAccountNumber(), transactionType, amount);

        // Send email for high-value transactions
        if (amount >= HIGH_TRANSACTION_AMOUNT) {
            sendEmailAlert(user.getEmail(), "High Value Transaction", alertMessage);
        }

        // Always send email for withdrawals and transfers
        if (transactionType.equals("WITHDRAWAL") || transactionType.contains("TRANSFER")) {
            sendEmailAlert(user.getEmail(), "Transaction Notification", alertMessage);
        }
    }

    public void sendSecurityAlert(String username, String action, String details, String email) {
        String alertMessage = String.format(
                "SECURITY ALERT\n\n" +
                        "User: %s\n" +
                        "Action: %s\n" +
                        "Details: %s\n" +
                        "Time: %s\n\n" +
                        "If you did not perform this action, please contact support immediately.",
                username, action, details, java.time.LocalDateTime.now()
        );

        logger.warn("Security alert - User: {}, Action: {}", username, action);
        sendEmailAlert(email, "Security Alert", alertMessage);
    }

    public void sendSuspiciousActivityAlert(Account account, User user, String activity, String details) {
        String alertMessage = String.format(
                "SUSPICIOUS ACTIVITY DETECTED\n\n" +
                        "Account: %s\n" +
                        "Activity: %s\n" +
                        "Details: %s\n" +
                        "Time: %s\n\n" +
                        "This activity has been flagged for review. " +
                        "If this was not you, please contact support immediately.",
                account.getAccountNumber(), activity, details, java.time.LocalDateTime.now()
        );

        logger.warn("Suspicious activity alert - Account: {}, Activity: {}",
                account.getAccountNumber(), activity);

        sendEmailAlert(user.getEmail(), "Suspicious Activity Detected", alertMessage);
    }

    public void sendWelcomeEmail(User user) {
        String welcomeMessage = String.format(
                "WELCOME TO BANKING TRANSACTION SIMULATOR\n\n" +
                        "Dear %s,\n\n" +
                        "Your account has been successfully created!\n" +
                        "Username: %s\n" +
                        "Email: %s\n" +
                        "Registration Date: %s\n\n" +
                        "Thank you for choosing our banking services.",
                user.getFullName(), user.getUsername(), user.getEmail(), user.getCreatedDate()
        );

        logger.info("Sending welcome email to: {}", user.getEmail());
        sendEmailAlert(user.getEmail(), "Welcome to Banking Simulator", welcomeMessage);
    }

    public void sendAccountCreatedAlert(User user, Account account) {
        String message = String.format(
                "NEW ACCOUNT CREATED\n\n" +
                        "Dear %s,\n\n" +
                        "A new account has been created successfully:\n" +
                        "Account Number: %s\n" +
                        "Account Type: %s\n" +
                        "Initial Balance: $%.2f\n" +
                        "Minimum Balance: $%.2f\n\n" +
                        "You can now start using your new account for transactions.",
                user.getFullName(), account.getAccountNumber(), account.getAccountType(),
                account.getBalance(), account.getMinBalance()
        );

        logger.info("Account creation alert sent for: {}", account.getAccountNumber());
        sendEmailAlert(user.getEmail(), "New Account Created", message);
    }

    // CHANGE FROM PRIVATE TO PUBLIC
    public void sendEmailAlert(String email, String subject, String message) {
        boolean success = emailService.sendEmail(email, subject, message);
        if (success) {
            logger.debug("Email alert sent successfully to: {}", email);
        } else {
            logger.warn("Failed to send email alert to: {}", email);
        }
    }

    // Getters for thresholds
    public double getLowBalanceThreshold() {
        return LOW_BALANCE_THRESHOLD;
    }

    public double getHighTransactionAmount() {
        return HIGH_TRANSACTION_AMOUNT;
    }

    public double getCriticalBalanceThreshold() {
        return CRITICAL_BALANCE_THRESHOLD;
    }

    public boolean isEmailEnabled() {
        return emailService.isEnabled();
    }
}