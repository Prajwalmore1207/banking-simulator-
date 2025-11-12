package com.banking.model;

import java.time.LocalDateTime;

public class Account {
    private String accountNumber;
    private int userId;
    private String accountHolderName;
    private double balance;
    private String accountType;
    private LocalDateTime createdDate;
    private double minBalance;
    private boolean isActive;

    public Account(String accountNumber, int userId, String accountHolderName,
                   double initialBalance, String accountType) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.createdDate = LocalDateTime.now();
        this.minBalance = 100.0;
        this.isActive = true;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public int getUserId() { return userId; }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getAccountType() { return accountType; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public double getMinBalance() { return minBalance; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("Account[%s] Holder: %s, Balance: $%.2f, Type: %s",
                accountNumber, accountHolderName, balance, accountType);
    }
}