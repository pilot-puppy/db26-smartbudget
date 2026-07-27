package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService svc;
    private IncomeTransaction income;
    private ExpenseTransaction expense;

    @BeforeEach
    void setUp() {
        svc = new TransactionService();
        income = new IncomeTransaction(1, new BigDecimal("3500"),
                LocalDate.of(2026, 1, 1), "Salary");
        expense = new ExpenseTransaction(2, new BigDecimal("45"),
                LocalDate.of(2026, 1, 5), "Groceries");
    }

    // TICKET-F040 — fresh SUT + fixtures per test
    @Test
    void initiallyEmpty() {
        assertEquals(0, svc.getAll().size());
        assertTrue(svc.getAll().isEmpty());
    }

    // TICKET-F041 — add then get
    @Test
    void addTransaction_isReturnedByGetAll() {
        IncomeTransaction t = new IncomeTransaction(1, new BigDecimal("100"), LocalDate.now(), "Test");
        svc.addTransaction(t);
        assertEquals(1, svc.getAll().size());
        assertEquals(1, svc.getAll().get(0).getTxnId());
    }

    // TICKET-F042 — delete removes it
    @Test
    void delete_removesTransaction() {
        svc.addTransaction(new IncomeTransaction(1, new BigDecimal("100"), LocalDate.now(), "Test"));
        assertEquals(1, svc.getAll().size());
        assertTrue(svc.delete(1));
        assertEquals(0, svc.getAll().size());
    }

    // TICKET-F043 — negative amount is rejected
    @Test
    void negativeAmount_throws() {
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class,
                () -> new IncomeTransaction(1, new BigDecimal("-10"), LocalDate.now(), "bad"));
        assertTrue(ex.getMessage().toLowerCase().contains("amount"));
    }
}
