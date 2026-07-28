package com.smartbudget.repository;

import com.smartbudget.entity.Transaction;
import com.smartbudget.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RepositoryDay5Test {

    @Autowired UserRepository userRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired TransactionRepository transactionRepository;

    // TICKET-F050
    @Test
    void findByEmail_returnsUserWhenPresent() {
        User alice = userRepository.findByEmail("alice@bank.com").orElseThrow();
        assertEquals("Alice Smith", alice.getName());
    }

    @Test
    void findByEmail_emptyWhenMissing() {
        assertTrue(userRepository.findByEmail("nobody@bank.com").isEmpty());
    }

    @Test
    void existsByEmail_trueOrFalse() {
        assertTrue(userRepository.existsByEmail("alice@bank.com"));
        assertFalse(userRepository.existsByEmail("nobody@bank.com"));
    }

    // TICKET-F052
    @Test
    void findByType_categoriesSplitByType() {
        assertEquals(2, categoryRepository.findByType("INCOME").size());
        assertEquals(3, categoryRepository.findByType("EXPENSE").size());
        assertTrue(categoryRepository.findByType("BOGUS").isEmpty());
    }

    // TICKET-F051
    @Test
    void findByUser_UserIdOrderByTxnDateDesc_newestFirst() {
        List<Transaction> txns = transactionRepository.findByUser_UserIdOrderByTxnDateDesc(1L);
        assertFalse(txns.isEmpty());
        for (int i = 1; i < txns.size(); i++) {
            assertFalse(txns.get(i).getTxnDate().isAfter(txns.get(i - 1).getTxnDate()));
        }
    }

    @Test
    void findByType_filtersIncomeAndExpense() {
        assertTrue(transactionRepository.findByType("INCOME").stream()
                .allMatch(t -> "INCOME".equals(t.getType())));
        assertTrue(transactionRepository.findByType("EXPENSE").stream()
                .allMatch(t -> "EXPENSE".equals(t.getType())));
    }

    @Test
    void findByTxnDateBetween_inclusiveRange() {
        List<Transaction> txns = transactionRepository.findByTxnDateBetween(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertFalse(txns.isEmpty());
        assertTrue(txns.stream().allMatch(t ->
                !t.getTxnDate().isBefore(LocalDate.of(2026, 5, 1))
                        && !t.getTxnDate().isAfter(LocalDate.of(2026, 5, 31))));
    }

    @Test
    void sumByUserAndType_coalesceReturnsZeroForMissingUser() {
        BigDecimal total = transactionRepository.sumByUserAndType(999L, "INCOME");
        assertNotNull(total);
        assertEquals(0, total.compareTo(BigDecimal.ZERO));
    }

    @Test
    void sumByUserAndType_sumsAliceIncome() {
        BigDecimal total = transactionRepository.sumByUserAndType(1L, "INCOME");
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    // TICKET-F053
    @Test
    void seedData_hasAtLeast25Transactions() {
        assertTrue(transactionRepository.count() >= 25);
    }
}
