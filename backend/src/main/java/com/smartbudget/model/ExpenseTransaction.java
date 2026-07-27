package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F023 (Day 3, Sprint 2) — Expense Transaction Subclass
// ============================================================
//
// WHAT: This is the sibling of IncomeTransaction. Both extend BaseTransaction.
//       While IncomeTransaction tracks "source" (where money came from),
//       ExpenseTransaction tracks "paymentMethod" (how money was spent).
//
// WHY:  Different transaction types carry different metadata.
//       Expenses need to know HOW payment was made (card, cash, bank transfer).
//       This demonstrates how inheritance lets siblings share a parent
//       while having their own unique data and behavior.
//
// ============================================================
public class ExpenseTransaction extends BaseTransaction {

    private String paymentMethod;

    public ExpenseTransaction(int txnId, BigDecimal amount, LocalDate txnDate,
                               String description) {
        this(txnId, amount, txnDate, description, null);
    }

    public ExpenseTransaction(int txnId, BigDecimal amount, LocalDate txnDate,
                               String description, String paymentMethod) {
        super(txnId, amount, txnDate, description);
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return super.toString() + " | paymentMethod=" + paymentMethod;
    }
}
