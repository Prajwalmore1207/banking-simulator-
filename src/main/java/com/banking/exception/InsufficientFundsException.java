package com.banking.exception;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String accountNumber, double balance, double amount) {
        super(String.format("Insufficient funds in account %s. Balance: $%.2f, Required: $%.2f",
                accountNumber, balance, amount));
    }
}