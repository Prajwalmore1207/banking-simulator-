package com.banking.engine;

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
    private final AccountManager accountManager;
    private final TransactionProcessor transactionProcessor;
    private final DatabaseManager databaseManager;

    // CSV headers
    private static final String ACCOUNT_SUMMARY_HEADER = "AccountNumber,AccountHolder,AccountType,Balance,CreatedDate,Status";
    private static final String TRANSACTION_HEADER = "TransactionID,Type,FromAccount,ToAccount,Amount,Timestamp,Status,Description";

    public ReportGenerator(AccountManager accountManager, TransactionProcessor transactionProcessor, DatabaseManager databaseManager) {
        this.accountManager = accountManager;
        this.transactionProcessor = transactionProcessor;
        this.databaseManager = databaseManager;
        logger.info("ReportGenerator initialized successfully");
    }

    public void generateAccountSummaryReport(String filename) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating account summary report: {}", csvFilename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            // Write CSV header
            writer.println(ACCOUNT_SUMMARY_HEADER);

            // Write note as a data row
            writer.println("NOTE,\"Full account reporting requires user authentication\",,,,\"Please use the application menu to view your accounts\"");

            logger.info("Account summary report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate account summary report: {}", csvFilename, e);
        }
    }

    public void generateUserAccountReport(String filename, int userId) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating user account report: {} for user: {}", csvFilename, userId);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            List<Account> accounts = accountManager.getUserAccounts(userId);

            // Write CSV header
            writer.println(ACCOUNT_SUMMARY_HEADER);

            double totalBalance = 0;
            for (Account account : accounts) {
                String csvLine = String.format("%s,%s,%s,%.2f,%s,%s",
                        escapeCSV(account.getAccountNumber()),
                        escapeCSV(account.getAccountHolderName()),
                        escapeCSV(account.getAccountType()),
                        account.getBalance(),
                        account.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        account.isActive() ? "Active" : "Inactive"
                );
                writer.println(csvLine);
                totalBalance += account.getBalance();
            }

            // Add summary row
            writer.println();
            writer.printf("TOTAL BALANCE,,,%.2f,,\"\"%n", totalBalance);

            logger.info("User account report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate user account report: {}", csvFilename, e);
        } catch (Exception e) {
            logger.error("Error generating user account report for user {}: {}", userId, e.getMessage());
        }
    }

    public void generateTransactionReport(String filename) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating transaction report: {}", csvFilename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            // Write CSV header
            writer.println(TRANSACTION_HEADER);

            // Write note as a data row
            writer.println("NOTE,\"Full transaction reporting requires account selection\",,,,,\"Please use the application menu to view transaction history\"");

            logger.info("Transaction report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate transaction report: {}", csvFilename, e);
        }
    }

    public void generateAccountTransactionReport(String filename, String accountNumber) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating account transaction report: {} for account: {}", csvFilename, accountNumber);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            List<Transaction> transactions = transactionProcessor.getAccountTransactions(accountNumber);

            // Write CSV header
            writer.println(TRANSACTION_HEADER);

            for (Transaction transaction : transactions) {
                String transactionType = transaction.getType() != null ? transaction.getType().toString() : "UNKNOWN";

                String csvLine = String.format("%s,%s,%s,%s,%.2f,%s,%s,%s",
                        escapeCSV(transaction.getTransactionId()),
                        escapeCSV(transactionType),
                        escapeCSV(transaction.getFromAccount() != null ? transaction.getFromAccount() : "N/A"),
                        escapeCSV(transaction.getToAccount() != null ? transaction.getToAccount() : "N/A"),
                        transaction.getAmount(),
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        transaction.isStatus() ? "SUCCESS" : "FAILED",
                        escapeCSV(transaction.getDescription())
                );
                writer.println(csvLine);
            }

            // Add summary row
            writer.println();
            writer.printf("SUMMARY,\"Total Transactions: %d\",,,,,,\"\"%n", transactions.size());

            logger.info("Account transaction report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate account transaction report: {}", csvFilename, e);
        } catch (Exception e) {
            logger.error("Error generating transaction report for account {}: {}", accountNumber, e.getMessage());
        }
    }

    // New method to generate comprehensive CSV report
    public void generateComprehensiveReport(String filename, int userId) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating comprehensive report: {} for user: {}", csvFilename, userId);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            List<Account> accounts = accountManager.getUserAccounts(userId);

            // Write accounts section
            writer.println("=== ACCOUNTS SUMMARY ===");
            writer.println(ACCOUNT_SUMMARY_HEADER);

            double totalBalance = 0;
            for (Account account : accounts) {
                String csvLine = String.format("%s,%s,%s,%.2f,%s,%s",
                        escapeCSV(account.getAccountNumber()),
                        escapeCSV(account.getAccountHolderName()),
                        escapeCSV(account.getAccountType()),
                        account.getBalance(),
                        account.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        account.isActive() ? "Active" : "Inactive"
                );
                writer.println(csvLine);
                totalBalance += account.getBalance();
            }

            writer.println();
            writer.printf("TOTAL BALANCE,,,%.2f,,\"\"%n", totalBalance);
            writer.println();

            // Write transactions section for each account
            for (Account account : accounts) {
                List<Transaction> transactions = transactionProcessor.getAccountTransactions(account.getAccountNumber());

                writer.println("=== TRANSACTIONS FOR ACCOUNT: " + account.getAccountNumber() + " ===");
                writer.println(TRANSACTION_HEADER);

                for (Transaction transaction : transactions) {
                    String transactionType = transaction.getType() != null ? transaction.getType().toString() : "UNKNOWN";

                    String csvLine = String.format("%s,%s,%s,%s,%.2f,%s,%s,%s",
                            escapeCSV(transaction.getTransactionId()),
                            escapeCSV(transactionType),
                            escapeCSV(transaction.getFromAccount() != null ? transaction.getFromAccount() : "N/A"),
                            escapeCSV(transaction.getToAccount() != null ? transaction.getToAccount() : "N/A"),
                            transaction.getAmount(),
                            transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            transaction.isStatus() ? "SUCCESS" : "FAILED",
                            escapeCSV(transaction.getDescription())
                    );
                    writer.println(csvLine);
                }
                writer.println();
                writer.printf("SUMMARY,\"Total Transactions: %d\",,,,,,\"\"%n", transactions.size());
                writer.println();
            }

            logger.info("Comprehensive report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate comprehensive report: {}", csvFilename, e);
        } catch (Exception e) {
            logger.error("Error generating comprehensive report for user {}: {}", userId, e.getMessage());
        }
    }

    // Utility method to ensure file has .csv extension
    private String ensureCSVExtension(String filename) {
        if (filename.toLowerCase().endsWith(".csv")) {
            return filename;
        }
        return filename + ".csv";
    }

    // Utility method to escape CSV special characters
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Method to generate report with custom date range
    public void generateDateRangeTransactionReport(String filename, String accountNumber,
                                                   String startDate, String endDate) {
        String csvFilename = ensureCSVExtension(filename);
        logger.info("Generating date range transaction report: {} for account: {} from {} to {}",
                csvFilename, accountNumber, startDate, endDate);

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilename))) {
            List<Transaction> transactions = transactionProcessor.getAccountTransactions(accountNumber);

            // Write CSV header with date range info
            writer.println("Date Range: " + startDate + " to " + endDate);
            writer.println(TRANSACTION_HEADER);

            int filteredCount = 0;
            for (Transaction transaction : transactions) {
                String transactionType = transaction.getType() != null ? transaction.getType().toString() : "UNKNOWN";

                String csvLine = String.format("%s,%s,%s,%s,%.2f,%s,%s,%s",
                        escapeCSV(transaction.getTransactionId()),
                        escapeCSV(transactionType),
                        escapeCSV(transaction.getFromAccount() != null ? transaction.getFromAccount() : "N/A"),
                        escapeCSV(transaction.getToAccount() != null ? transaction.getToAccount() : "N/A"),
                        transaction.getAmount(),
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        transaction.isStatus() ? "SUCCESS" : "FAILED",
                        escapeCSV(transaction.getDescription())
                );
                writer.println(csvLine);
                filteredCount++;
            }

            // Add summary row
            writer.println();
            writer.printf("SUMMARY,\"Filtered Transactions: %d\",,,,,,\"\"%n", filteredCount);

            logger.info("Date range transaction report generated successfully: {}", csvFilename);

        } catch (IOException e) {
            logger.error("Failed to generate date range transaction report: {}", csvFilename, e);
        } catch (Exception e) {
            logger.error("Error generating date range transaction report for account {}: {}", accountNumber, e.getMessage());
        }
    }
}