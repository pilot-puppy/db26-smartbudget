package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        assertEquals(0, svc.size());
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

    @Test
    void addTransaction_multipleItems_allReturned() {
        svc.addTransaction(income);
        svc.addTransaction(expense);
        assertEquals(2, svc.size());
    }

    @Test
    void getAll_returnsDefensiveCopy() {
        svc.addTransaction(income);
        svc.getAll().clear();
        assertEquals(1, svc.size(), "clearing the returned list must not affect the service");
    }

    // TICKET-F042 — delete removes it
    @Test
    void delete_removesTransaction() {
        svc.addTransaction(new IncomeTransaction(1, new BigDecimal("100"), LocalDate.now(), "Test"));
        assertEquals(1, svc.getAll().size());
        assertTrue(svc.delete(1));
        assertEquals(0, svc.getAll().size());
    }

    @Test
    void delete_missingId_returnsFalse() {
        assertFalse(svc.delete(999), "deleting an unknown id is not an error, just false");
    }

    // TICKET-F043 — negative amount is rejected
    @Test
    void negativeAmount_throws() {
        InvalidTransactionException ex = assertThrows(InvalidTransactionException.class,
                () -> new IncomeTransaction(1, new BigDecimal("-10"), LocalDate.now(), "bad"));
        assertTrue(ex.getMessage().toLowerCase().contains("amount"));
    }

    // TICKET-F043 — the service itself rejects a blank description (F031 validation)
    @Test
    void blankDescription_throwsAndStoresNothing() {
        IncomeTransaction bad = new IncomeTransaction(9, new BigDecimal("10"), LocalDate.now(), "  ");
        assertThrows(InvalidTransactionException.class, () -> svc.addTransaction(bad));
        assertEquals(0, svc.size(), "invalid input must never reach storage");
    }

    // TICKET-F032 — HashMap storage gives O(1) lookup by id
    @Test
    void findById_locatesTransaction() {
        svc.addTransaction(income);
        svc.addTransaction(expense);
        assertNotNull(svc.findById("1"));
        assertEquals("INCOME", svc.findById(1).getType());
        assertNull(svc.findById(404), "unknown id returns null");
    }

    // TICKET-F033 — Stream filtering
    @Test
    void getExpensesOver100_keepsOnlyBigExpenses() {
        svc.addTransaction(income);   // INCOME 3500 — excluded by type
        svc.addTransaction(expense);  // EXPENSE 45  — excluded by amount
        svc.addTransaction(new ExpenseTransaction(3, new BigDecimal("250"),
                LocalDate.of(2026, 2, 3), "Flight"));

        List<BaseTransaction> result = svc.getExpensesOver100();
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getTxnId());
    }

    // TICKET-F033 — Stream sorting by date
    @Test
    void getSortedByDate_ascendingThenDescending() {
        svc.addTransaction(expense);  // 2026-01-05
        svc.addTransaction(income);   // 2026-01-01

        assertEquals(1, svc.getSortedByDate().get(0).getTxnId());
        assertEquals(2, svc.getSortedByDateDesc().get(0).getTxnId());
    }

    // TICKET-F034 — Lambda comparator, highest amount first
    @Test
    void getSortedByAmount_highestFirst() {
        svc.addTransaction(expense);  // 45
        svc.addTransaction(income);   // 3500

        List<BaseTransaction> sorted = svc.getSortedByAmount();
        assertEquals(new BigDecimal("3500"), sorted.get(0).getAmount());
        assertEquals(new BigDecimal("45"), sorted.get(1).getAmount());
    }

    // TICKET-F028 — totals per type still work after the F032 refactor
    @Test
    void calculateTotalByType_sumsPerType() {
        svc.addTransaction(income);
        svc.addTransaction(expense);
        assertEquals(new BigDecimal("3500"), svc.calculateTotalByType("INCOME"));
        assertEquals(new BigDecimal("45"), svc.calculateTotalByType("EXPENSE"));
        assertEquals(BigDecimal.ZERO, svc.calculateTotalByType("UNKNOWN"));
    }

    // TICKET-F027 — date-range filter still works after the F032 refactor
    @Test
    void filterByDateRange_inclusiveBounds() {
        svc.addTransaction(income);   // 2026-01-01
        svc.addTransaction(expense);  // 2026-01-05

        assertEquals(2, svc.filterByDateRange(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)).size());
        assertEquals(1, svc.filterByDateRange(
                LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 31)).size());
        assertTrue(svc.filterByDateRange(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 1, 1)).isEmpty());
    }
}
