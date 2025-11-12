package com.banking.model;

import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private double amount;
    private TransactionType type;
    private LocalDateTime timestamp;
    private String description;
    private boolean status;

    public Transaction(String transactionId, String fromAccount, String toAccount,
                       double amount, TransactionType type, String description) {
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.status = true;
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Transaction[%s]: %s - Amount: $%.2f, Time: %s",
                transactionId, type, amount, timestamp);
    }
}