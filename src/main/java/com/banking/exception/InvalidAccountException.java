package com.banking.exception;

public class InvalidAccountException extends Exception {
    public InvalidAccountException(String message) {
        super(message);
    }

    // Remove the duplicate constructor that takes only accountNumber
    // The single constructor above can handle both cases
}