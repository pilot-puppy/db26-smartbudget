package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F022 (Day 3, Sprint 2) — Income Transaction Subclass
// ============================================================
//
// WHAT: This class EXTENDS BaseTransaction, which means it inherits all of
//       BaseTransaction's fields (txnId, amount, txnDate, description) and methods.
//       "extends" creates a parent-child (IS-A) relationship:
//       "An IncomeTransaction IS-A BaseTransaction."
//
// WHY:  Income transactions have an extra field: "source" (where the money came from).
//       Instead of duplicating all the common fields, we inherit them from the parent
//       and just add what's unique to income.
//
// ============================================================
public class IncomeTransaction extends BaseTransaction {

    private String source;

    public IncomeTransaction(int txnId, BigDecimal amount, LocalDate txnDate,
                              String description) {
        this(txnId, amount, txnDate, description, null);
    }

    public IncomeTransaction(int txnId, BigDecimal amount, LocalDate txnDate,
                              String description, String source) {
        super(txnId, amount, txnDate, description);
        this.source = source;
    }

    @Override
    public String getType() {
        return "INCOME";
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return super.toString() + " | source=" + source;
    }
}
