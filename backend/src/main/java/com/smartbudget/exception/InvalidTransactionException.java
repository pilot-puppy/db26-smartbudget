package com.smartbudget.exception;

// ============================================================
// TICKET-F024 (Day 3, Sprint 2) — Custom Exception Class
// ============================================================
//
// WHAT: A custom exception is a class that extends RuntimeException.
//       It lets you throw meaningful errors with clear messages.
//       When thrown, it stops execution and reports what went wrong.
//
//       RuntimeException is "unchecked" — you don't need to declare it
//       in method signatures with "throws". Java has two types:
//         - Checked exceptions (must declare with "throws") — for recoverable errors
//         - Unchecked/Runtime exceptions (no declaration needed) — for programming errors
//
// WHY:  Without a custom exception, you'd throw generic RuntimeException("Amount invalid").
//       With InvalidTransactionException, your code is more readable and the
//       GlobalExceptionHandler (Day 6) can catch it specifically and return HTTP 400.
//
//       Using it in BaseTransaction constructor:
//         if amount <= 0 → throw new InvalidTransactionException("Amount must be greater than zero")
//         if date is future → throw new InvalidTransactionException("Date cannot be in the future")
//
// ============================================================
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
