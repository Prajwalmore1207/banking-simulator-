package com.banking;

import com.banking.model.User;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.engine.AccountManager;
import com.banking.engine.TransactionProcessor;
import com.banking.service.AuthService;
import com.banking.engine.ReportGenerator;
import com.banking.service.AlertService;
import com.banking.service.BalanceTracker;
import com.banking.database.DatabaseManager;
import com.banking.database.DatabaseConnection;
import com.banking.exception.*;
import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

public class BankingSimulator {
    private static final Logger logger = LoggerUtil.getLogger(BankingSimulator.class);

    private AuthService authService;
    private AccountManager accountManager;
    private TransactionProcessor transactionProcessor;
    private ReportGenerator reportGenerator;
    private AlertService alertService;
    private BalanceTracker balanceTracker;
    private DatabaseManager databaseManager;
    private Scanner scanner;
    private User currentUser;

    public BankingSimulator() {
        initializeComponents();
        this.scanner = new Scanner(System.in);
    }

    private void initializeComponents() {
        logger.info("Initializing Banking Simulator Components...");

        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            logger.error("Database connection failed.");
            System.err.println("CRITICAL: Database connection failed. Exiting.");
            System.exit(1);
        }

        this.databaseManager = new DatabaseManager();
        this.authService = new AuthService(databaseManager);
        this.accountManager = new AccountManager(databaseManager);

        // Initialize transactionProcessor with -1 (no user) initially
        this.transactionProcessor = new TransactionProcessor(accountManager, databaseManager, -1);

        this.alertService = new AlertService(databaseManager);
        this.reportGenerator = new ReportGenerator(accountManager, transactionProcessor, databaseManager);
        this.balanceTracker = new BalanceTracker(accountManager, alertService, databaseManager);

        logger.info("All Banking Simulator components initialized successfully");
    }

    public void start() {
        logger.info("=== BANKING SIMULATOR STARTED ===");
        System.out.println("Welcome to Java Banking Transaction Simulator!");

        boolean running = true;

        while (running) {
            try {
                if (!authService.isAuthenticated()) {
                    showMainMenu();
                } else {
                    showUserMenu();
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                logger.error("Error in main application loop", e);
                // Continue running despite errors
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== BANKING SIMULATOR ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Reset Password");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    handleRegistration();
                    break;
                case 2:
                    handleLogin();
                    break;
                case 3:
                    handlePasswordReset();
                    break;
                case 4:
                    shutdown();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            logger.error("Error in main menu", e);
        }
    }

    private void showUserMenu() {
        System.out.println("\n=== USER DASHBOARD ===");
        System.out.println("Welcome, " + currentUser.getFullName() + "!");
        System.out.println("1. Create Account");
        System.out.println("2. View Accounts");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        System.out.println("6. View Transaction History");
        System.out.println("7. Generate Reports");
        System.out.println("8. Check Balance Alerts");
        System.out.println("9. Change Password");
        System.out.println("10. Logout");
        System.out.print("Choose an option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    handleCreateAccount();
                    break;
                case 2:
                    handleViewAccounts();
                    break;
                case 3:
                    handleDeposit();
                    break;
                case 4:
                    handleWithdraw();
                    break;
                case 5:
                    handleTransfer();
                    break;
                case 6:
                    handleViewTransactions();
                    break;
                case 7:
                    handleGenerateReports();
                    break;
                case 8:
                    handleBalanceAlerts();
                    break;
                case 9:
                    handleChangePassword();
                    break;
                case 10:
                    handleLogout();
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            logger.error("Error in user menu", e);
        }
    }

    private void handleRegistration() {
        System.out.println("\n=== USER REGISTRATION ===");

        try {
            // Get user details directly using Scanner
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Phone Number: ");
            String phoneNumber = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            System.out.print("Confirm Password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!password.equals(confirmPassword)) {
                System.out.println("✗ Error: Passwords do not match!");
                return;
            }

            // Call the registerUser method directly
            User newUser = authService.registerUser(username, password, email, fullName, phoneNumber);

            if (newUser != null) {
                // Send welcome email
                alertService.sendWelcomeEmail(newUser);
                System.out.println("✓ Registration successful! Please login to continue.");
                System.out.println("📧 Welcome email has been sent to: " + newUser.getEmail());
            }

        } catch (AuthenticationException e) {
            System.out.println("✗ Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during registration.");
            logger.error("Registration error", e);
        }
    }

    private void handleLogin() {
        System.out.println("\n=== USER LOGIN ===");

        try {
            // Get login details directly using Scanner
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            // Call the login method directly
            User user = authService.login(username, password);
            this.currentUser = user;

            // Update transaction processor with current user ID
            this.transactionProcessor.setCurrentUserId(user.getUserId());

            System.out.println("✓ Login successful! Welcome, " + user.getFullName());

        } catch (AuthenticationException e) {
            System.out.println("✗ Login failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during login.");
            logger.error("Login error", e);
        }
    }

    private void handlePasswordReset() {
        System.out.println("\n=== PASSWORD RESET ===");

        try {
            // Get reset details directly using Scanner
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();

            System.out.print("Confirm New Password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("✗ Error: Passwords do not match!");
                return;
            }

            // Verify user exists and email matches
            User user = databaseManager.getUserByUsername(username);
            if (user == null || !user.getEmail().equalsIgnoreCase(email)) {
                System.out.println("✗ Error: Invalid username or email!");
                return;
            }

            // Hash new password
            String newHashedPassword = com.banking.util.PasswordUtil.hashPassword(newPassword);

            // Update password in database
            boolean success = databaseManager.updatePassword(user.getUserId(), newHashedPassword);

            if (success) {
                System.out.println("✓ Password reset successfully! You can now login with your new password.");
                logger.info("Password reset for user: {}", username);
            } else {
                System.out.println("✗ Failed to reset password!");
                logger.error("Password reset failed for user: {}", username);
            }

        } catch (Exception e) {
            System.out.println("An unexpected error occurred during password reset.");
            logger.error("Password reset error", e);
        }
    }

    private void handleChangePassword() {
        System.out.println("\n=== CHANGE PASSWORD ===");

        try {
            // Get password change details directly using Scanner
            System.out.print("Current Password: ");
            String currentPassword = scanner.nextLine();

            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();

            System.out.print("Confirm New Password: ");
            String confirmPassword = scanner.nextLine();

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("✗ Error: New passwords do not match!");
                return;
            }

            // Verify current password
            if (!com.banking.util.PasswordUtil.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
                System.out.println("✗ Error: Current password is incorrect!");
                return;
            }

            // Validate password strength
            if (!com.banking.util.PasswordUtil.isStrongPassword(newPassword)) {
                System.out.println("✗ Error: New password does not meet strength requirements!");
                return;
            }

            // Hash new password
            String newHashedPassword = com.banking.util.PasswordUtil.hashPassword(newPassword);

            // Update password in database
            boolean success = databaseManager.updatePassword(currentUser.getUserId(), newHashedPassword);

            if (success) {
                // Update current user object
                currentUser.setPasswordHash(newHashedPassword);
                System.out.println("✓ Password changed successfully!");
                logger.info("Password changed for user: {}", currentUser.getUsername());
            } else {
                System.out.println("✗ Failed to change password!");
                logger.error("Password change failed for user: {}", currentUser.getUsername());
            }

        } catch (Exception e) {
            System.out.println("Error changing password: " + e.getMessage());
            logger.error("Password change error for user {}: {}", currentUser.getUsername(), e.getMessage(), e);
        }
    }

    // Rest of your methods remain unchanged...
    private void handleCreateAccount() {
        System.out.println("\n=== CREATE ACCOUNT ===");

        try {
            System.out.print("Initial Deposit: ");
            double initialDeposit = Double.parseDouble(scanner.nextLine());

            if (initialDeposit < 100) {
                System.out.println("Minimum initial deposit is $100.00");
                return;
            }

            System.out.print("Account Type (SAVINGS/CURRENT): ");
            String accountType = scanner.nextLine().toUpperCase();

            if (!accountType.equals("SAVINGS") && !accountType.equals("CURRENT")) {
                System.out.println("Invalid account type. Using SAVINGS as default.");
                accountType = "SAVINGS";

            }

            // Generate account number
            String accountNumber = "ACC" + System.currentTimeMillis();

            Account account = new Account(accountNumber, currentUser.getUserId(),
                    currentUser.getFullName(), initialDeposit, accountType);

            accountManager.createAccount(account);
            alertService.sendAccountCreatedAlert(currentUser, account);

            // Check initial balance
            balanceTracker.checkNewAccountBalance(account, currentUser);

            System.out.println("Account created successfully!");
            System.out.println("Your account number: " + accountNumber);
            System.out.printf("Initial Balance: $%.2f%n", initialDeposit);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (InvalidAccountException e) {
            System.out.println("Failed to create account: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            logger.error("Account creation error", e);
        }
    }

    private void handleViewAccounts() {
        System.out.println("\n=== YOUR ACCOUNTS ===");

        try {
            List<Account> accounts = accountManager.getUserAccounts(currentUser.getUserId());

            if (accounts.isEmpty()) {
                System.out.println("No accounts found.");
                return;
            }

            System.out.println("Your Accounts:");
            for (Account account : accounts) {
                System.out.printf("• Account: %s, Balance: $%.2f, Type: %s%n",
                        account.getAccountNumber(), account.getBalance(), account.getAccountType());
            }

            double totalBalance = 0;
            for (Account account : accounts) {
                totalBalance += account.getBalance();
            }
            System.out.printf("%nTotal Balance Across All Accounts: $%.2f%n", totalBalance);

        } catch (Exception e) {
            System.out.println("An error occurred while fetching accounts.");
            logger.error("Error viewing accounts", e);
        }
    }

    private void handleDeposit() {
        System.out.println("\n=== DEPOSIT ===");

        try {
            System.out.print("Account Number: ");
            String accountNumber = scanner.nextLine();

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            Transaction transaction = transactionProcessor.deposit(accountNumber, amount);

            System.out.println("Deposit successful!");
            System.out.println("Transaction ID: " + transaction.getTransactionId());
            System.out.printf("New Balance: $%.2f%n",
                    accountManager.getAccountBalance(accountNumber));

            // Send alert - pass both account and current user
            Account account = accountManager.getAccount(accountNumber);
            alertService.sendTransactionAlert(account, currentUser, "DEPOSIT", amount);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (InvalidAccountException | InvalidTransactionException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred.");
            logger.error("Deposit error", e);
        }
    }

    private void handleWithdraw() {
        System.out.println("\n=== WITHDRAW ===");

        try {
            System.out.print("Account Number: ");
            String accountNumber = scanner.nextLine();

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            Transaction transaction = transactionProcessor.withdraw(accountNumber, amount);

            System.out.println("Withdrawal successful!");
            System.out.println("Transaction ID: " + transaction.getTransactionId());
            System.out.printf("New Balance: $%.2f%n",
                    accountManager.getAccountBalance(accountNumber));

            // Send alert - pass both account and current user
            Account account = accountManager.getAccount(accountNumber);
            alertService.sendTransactionAlert(account, currentUser, "WITHDRAWAL", amount);
            balanceTracker.checkTransactionThreshold(account, currentUser, amount, "WITHDRAWAL");

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (InvalidAccountException | InsufficientFundsException | InvalidTransactionException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred.");
            logger.error("Withdrawal error", e);
        }
    }

    private void handleTransfer() {
        System.out.println("\n=== TRANSFER ===");

        try {
            System.out.print("From Account: ");
            String fromAccount = scanner.nextLine();

            System.out.print("To Account: ");
            String toAccount = scanner.nextLine();

            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            Transaction transaction = transactionProcessor.transfer(fromAccount, toAccount, amount);

            System.out.println("Transfer successful!");
            System.out.println("Transaction ID: " + transaction.getTransactionId());
            System.out.printf("From Account Balance: $%.2f%n",
                    accountManager.getAccountBalance(fromAccount));

            Account sourceAccount = accountManager.getAccount(fromAccount);
            alertService.sendTransactionAlert(sourceAccount, currentUser, "TRANSFER_OUT", amount);
            balanceTracker.checkTransactionThreshold(sourceAccount, currentUser, amount, "TRANSFER");

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid amount.");
        } catch (InvalidAccountException | InsufficientFundsException | InvalidTransactionException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred.");
            logger.error("Transfer error", e);
        }
    }

    private void handleViewTransactions() {
        System.out.println("\n=== TRANSACTION HISTORY ===");

        try {
            System.out.print("Account Number: ");
            String accountNumber = scanner.nextLine();

            List<Transaction> transactions = transactionProcessor.getAccountTransactions(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for this account.");
                return;
            }

            System.out.println("Transaction History for Account: " + accountNumber);
            System.out.println("------------------------------------------------");

            for (Transaction transaction : transactions) {
                System.out.printf("Date: %s%n", transaction.getTimestamp().toLocalDate());
                System.out.printf("Type: %s%n", transaction.getType());
                System.out.printf("Amount: $%.2f%n", transaction.getAmount());
                System.out.printf("Description: %s%n", transaction.getDescription());
                System.out.printf("Transaction ID: %s%n", transaction.getTransactionId());

                if (transaction.getType() == com.banking.model.TransactionType.TRANSFER) {
                    System.out.printf("From: %s%n", transaction.getFromAccount());
                    System.out.printf("To: %s%n", transaction.getToAccount());
                }
                System.out.println("------------------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("An error occurred while fetching transactions.");
            logger.error("Error viewing transactions", e);
        }
    }

    private void handleGenerateReports() {
        System.out.println("\n=== GENERATE REPORTS ===");
        System.out.println("1. Account Summary Report");
        System.out.println("2. Transaction Report");
        System.out.print("Choose report type: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    reportGenerator.generateUserAccountReport(
                            "reports/account_summary_" + currentUser.getUserId() + ".txt",
                            currentUser.getUserId()
                    );
                    System.out.println("Account summary report generated in 'reports' folder.");
                    break;
                case 2:
                    System.out.print("Enter Account Number for transaction report: ");
                    String accountNumber = scanner.nextLine();
                    reportGenerator.generateAccountTransactionReport(
                            "reports/transaction_report_" + accountNumber + ".txt",
                            accountNumber
                    );
                    System.out.println("Transaction report generated in 'reports' folder.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("An error occurred while generating reports.");
            logger.error("Report generation error", e);
        }
    }

    private void handleBalanceAlerts() {
        System.out.println("\n=== BALANCE ALERTS ===");
        try {
            balanceTracker.monitorUserAccounts(currentUser.getUserId(), currentUser);
            balanceTracker.generateBalanceReport(currentUser.getUserId());

            System.out.println("Balance check completed. Check application logs for details.");
        } catch (Exception e) {
            System.out.println("An error occurred during balance check.");
            logger.error("Balance alert error", e);
        }
    }

    private void handleLogout() {
        authService.logout();
        this.currentUser = null;
        // Reset transaction processor to no user
        this.transactionProcessor.setCurrentUserId(-1);
        accountManager.clearCache();
        System.out.println("Logged out successfully.");
    }

    private void shutdown() {
        System.out.println("Shutting down Banking Simulator...");
        DatabaseConnection.closeConnection();
        scanner.close();
        logger.info("=== BANKING SIMULATOR SHUTDOWN ===");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            BankingSimulator simulator = new BankingSimulator();

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down gracefully...");
                DatabaseConnection.closeConnection();
            }));

            simulator.start();

        } catch (Exception e) {
            LoggerUtil.getLogger(BankingSimulator.class).error("Fatal error in Banking Simulator", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}