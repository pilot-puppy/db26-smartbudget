package com.smartbudget.console;

// ============================================================
// TICKET-F025 (Day 3, Sprint 2) — Polymorphism Demo
// ============================================================
//
// Builds a List<BaseTransaction> holding both IncomeTransaction and
// ExpenseTransaction instances, then loops over it calling getType()/toString()
// with no instanceof checks — proving dynamic dispatch.
// ============================================================

import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PolymorphismDemo {
    public static void main(String[] args) {
        List<BaseTransaction> mixed = new ArrayList<>();
        mixed.add(new IncomeTransaction(1, new BigDecimal("3500"), LocalDate.now(), "Salary", "Company"));
        mixed.add(new ExpenseTransaction(2, new BigDecimal("45"), LocalDate.now(), "Groceries", "CARD"));
        mixed.add(new IncomeTransaction(3, new BigDecimal("800"), LocalDate.now(), "Freelance", "Client"));
        mixed.add(new ExpenseTransaction(4, new BigDecimal("120"), LocalDate.now(), "Bills", "BANK_TRANSFER"));

        System.out.println("Total rows: " + mixed.size());
        for (BaseTransaction t : mixed) {
            System.out.println(t.getType() + " -> " + t);
        }

        BigDecimal income = BigDecimal.ZERO;
        for (BaseTransaction t : mixed) {
            if ("INCOME".equals(t.getType())) income = income.add(t.getAmount());
        }
        System.out.println("Total income: " + income);
    }
}
