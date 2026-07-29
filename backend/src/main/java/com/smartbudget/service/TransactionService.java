package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartbudget.entity.*;
import com.smartbudget.exception.*;
import com.smartbudget.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// ============================================================
// TransactionService — evolves across THREE days:
//
// Day 3 (Sprint 2) — TICKET-F026 to F030: Plain Java service with List
// Day 4 (Sprint 3) — TICKET-F032 to F034: Refactor with HashMap + Streams + Lambdas
// Day 6 (Sprint 5) — TICKET-F063:         Spring @Service using JPA repositories
//
// Each day BUILDS on the previous — don't delete old code, evolve it.
// ============================================================
@Service
public class TransactionService {

    // ==========================================================
    //  DAY 3 (Sprint 2): Plain Java with ArrayList
    //  → storage was refactored to a Map by TICKET-F032 (see Day 4 below);
    //    the Day 3 methods below now read from transactions.values().
    // ==========================================================

    // LinkedHashMap (a HashMap subclass) keeps insertion order, so getAll()
    // and exportToCSV stay reproducible across runs.
    private final Map<String, BaseTransaction> transactions = new LinkedHashMap<>();

    public void addTransaction(BaseTransaction t) {
        if (t == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new InvalidTransactionException(
                    "description must not be blank for transaction id=" + t.getTxnId());
        }
        transactions.put(String.valueOf(t.getTxnId()), t);
    }

    public List<BaseTransaction> getAll() {
        // defensive copy so callers can't mutate our internal storage
        return new ArrayList<>(transactions.values());
    }

    public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must be non-null");
        }
        if (from.isAfter(to)) {
            return new ArrayList<>();
        }
        List<BaseTransaction> result = new ArrayList<>();
        for (BaseTransaction t : transactions.values()) {
            LocalDate d = t.getTxnDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                result.add(t);
            }
        }
        return result;
    }

    public BigDecimal calculateTotalByType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BaseTransaction t : transactions.values()) {
            if (type.equals(t.getType())) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    public void exportToCSV(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("id,type,amount,date,description");
            bw.newLine();
            for (BaseTransaction t : transactions.values()) {
                bw.write(String.join(",",
                        String.valueOf(t.getTxnId()),
                        t.getType(),
                        t.getAmount().toPlainString(),
                        t.getTxnDate().toString(),
                        csvEscape(t.getDescription())
                ));
                bw.newLine();
            }
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public void importFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line == null) return;

            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split(",", -1);
                    int id = Integer.parseInt(parts[0].trim());
                    String type = parts[1].trim();
                    BigDecimal amount = new BigDecimal(parts[2].trim());
                    LocalDate date = LocalDate.parse(parts[3].trim());
                    String desc = parts.length > 4 ? parts[4] : "";

                    BaseTransaction t = switch (type) {
                        case "INCOME" -> new IncomeTransaction(id, amount, date, desc, null);
                        case "EXPENSE" -> new ExpenseTransaction(id, amount, date, desc, null);
                        default -> throw new InvalidTransactionException(
                                "Unknown type on line " + lineNum + ": " + type);
                    };
                    transactions.put(String.valueOf(t.getTxnId()), t);
                } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
                    System.err.println("Skipping bad row at line " + lineNum + ": " + e.getMessage());
                }
            }
        }
    }


    // ==========================================================
    //  DAY 4 (Sprint 3): Refactor with HashMap + Streams + Lambdas
    // ==========================================================

    // -------------------------------------------------------
    // TODO TICKET-F032: Refactor storage from List → HashMap
    // -------------------------------------------------------
    // WHAT: A HashMap stores key-value pairs. Unlike a List (which uses index 0, 1, 2...),
    //       a HashMap uses meaningful keys (like transaction ID as String).
    //       Lookup by key is O(1) — instant, regardless of size.
    //
    // HOW:  Replace your List<BaseTransaction> field with Map<String, BaseTransaction>.
    //       Update addTransaction: use map.put(String.valueOf(t.getTxnId()), t)
    //       Update getAll: return new ArrayList<>(map.values())
    //
    // WHY:  HashMap enables O(1) lookups by ID. With a List, finding a transaction by ID
    //       requires looping through every element (O(n)). This matters at scale.
    //
    // OBSERVE: All existing functionality should still work — just faster lookups by ID.

    public BaseTransaction findById(String id) {
        return transactions.get(id);
    }

    public BaseTransaction findById(int txnId) {
        return findById(String.valueOf(txnId));
    }

    public boolean delete(String id) {
        return transactions.remove(id) != null;
    }

    public boolean delete(int txnId) {
        return delete(String.valueOf(txnId));
    }

    public int size() {
        return transactions.size();
    }

    // -------------------------------------------------------
    // TODO TICKET-F033: Add Stream-based filtering
    // -------------------------------------------------------
    // WHAT: Streams are Java's functional programming feature (added in Java 8).
    //       Instead of writing for-loops, you chain operations: filter → sort → collect.
    //       Streams are declarative — you say WHAT you want, not HOW to do it.
    //
    // HOW:  Create two methods:
    //       1. getExpensesOver100(): Use .stream().filter() to keep only expenses with amount > 100.
    //          Chain: collection.stream() → .filter(condition1) → .filter(condition2) → .collect(toList())
    //       2. getSortedByDate(): Use .stream().sorted() with Comparator.comparing().
    //          Chain: collection.stream() → .sorted(Comparator.comparing(BaseTransaction::getTxnDate)) → .collect(toList())
    //
    // WHY:  Streams are more concise and readable than for-loops for data processing.
    //       They're also parallelizable — Java can automatically process large datasets across CPU cores.
    //
    // OBSERVE: Compare the Stream version with the for-loop version (Day 3).
    //          Same result, fewer lines, more readable.

    public List<BaseTransaction> getExpensesOver100() {
        BigDecimal threshold = new BigDecimal("100");
        return transactions.values().stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDate() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDateDesc() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate).reversed())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // TODO TICKET-F034: Lambda Comparator for custom sorting
    // -------------------------------------------------------
    // WHAT: A Lambda is an anonymous function — a function without a name.
    //       (a, b) -> b.getAmount().compareTo(a.getAmount()) is a Lambda that compares two transactions.
    //       This replaces creating a separate Comparator class.
    //
    // HOW:  Create getSortedByAmount() that uses .sorted() with a lambda comparator.
    //       For descending order: (a, b) -> b.getAmount().compareTo(a.getAmount())
    //       For ascending order: (a, b) -> a.getAmount().compareTo(b.getAmount())
    //
    // WHY:  Before Java 8, sorting required creating anonymous inner classes (verbose).
    //       Lambdas do the same thing in one line.
    //
    // OBSERVE: Call getSortedByAmount() — the first item should have the highest amount.

    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }


    // ==========================================================
    //  DAY 6 (Sprint 5): Spring @Service with JPA Repositories
    // ==========================================================

    // -------------------------------------------------------
    // TODO TICKET-F063: Step 1 — Add @Service annotation + inject repositories
    // -------------------------------------------------------
    // WHAT: @Service tells Spring: "This is a business logic class. Create one instance
    //       and manage its lifecycle." Spring then automatically injects (provides)
    //       the repositories this service needs through the constructor.
    //       This is called Dependency Injection (DI).
    //
    // HOW:  Add @Service above the class declaration.
    //       Declare three private final fields: TransactionRepository, UserRepository, CategoryRepository.
    //       Create a constructor that accepts all three and assigns them.
    //       Spring automatically calls this constructor and passes the repository beans.
    //
    // WHY:  Separating controllers (HTTP handling) from services (business logic) follows
    //       the Single Responsibility Principle. Controllers handle requests,
    //       services handle rules, repositories handle data access.
    //
    // OBSERVE: The app should still boot. Check Spring logs for "Creating bean: transactionService".
    private final TransactionRepository txnRepo;
    private final UserRepository        userRepo;
    private final CategoryRepository    categoryRepo;

    // Spring uses this constructor (only one annotated) to inject the JPA repositories.
    @org.springframework.beans.factory.annotation.Autowired
    public TransactionService(TransactionRepository txnRepo,
                              UserRepository userRepo,
                              CategoryRepository categoryRepo) {
        this.txnRepo      = txnRepo;
        this.userRepo     = userRepo;
        this.categoryRepo = categoryRepo;
    }

    // No-arg constructor kept for the Day 3/4 in-memory usage (console app, unit tests)
    // that predates the Day 6 JPA repositories.
    public TransactionService() {
        this.txnRepo      = null;
        this.userRepo     = null;
        this.categoryRepo = null;
    }
    // -------------------------------------------------------
    // TODO TICKET-F063: Step 2 — Implement CRUD methods
    // -------------------------------------------------------
    // WHAT: The service wraps repository calls with validation and error handling.
    //       Methods: getAll(), getById(), getByUserId(), create(), update(), delete()
    //
    // HOW:  For getAll(): simply delegate to repo.findAll()
    //       For getById(): use repo.findById(id).orElseThrow() — throw ResourceNotFoundException if missing
    //       For create(): validate amount > 0 (throw InvalidTransactionException if not),
    //         look up the User and Category by ID (throw ResourceNotFoundException if missing),
    //         build a Transaction entity, save it with repo.save()
    //       For delete(): check repo.existsById(id) first, throw ResourceNotFoundException if false
    //
    // WHY:  The repository has no business rules — repo.save() accepts ANY data.
    //       The service adds validation before saving, ensuring bad data never reaches the database.
    //       orElseThrow() is a clean way to handle "not found" cases without null checks.
    //
    // OBSERVE: After implementing, build TransactionController to use this service.
    //          POST a transaction with amount = -10 → should get HTTP 400.
    //          GET a non-existent ID → should get HTTP 404.
    //          These responses come from the exceptions caught by GlobalExceptionHandler.
    // Named getAllTransactions() (not getAll()) to avoid colliding with the
    // Day 3/4 in-memory getAll() above, which returns List<BaseTransaction>.
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return txnRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        return txnRepo.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getByUserId(Long userId) {
        return txnRepo.findByUser_UserIdOrderByTxnDateDesc(userId);
    }

    @Transactional
    public Transaction create(Long userId, Long categoryId,
                              BigDecimal amount, LocalDate date,
                              String description, String type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("amount must be > 0");
        }
        if (type == null || (!"INCOME".equals(type) && !"EXPENSE".equals(type))) {
            throw new InvalidTransactionException(
                "type must be 'INCOME' or 'EXPENSE'");
        }
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("date cannot be in the future");
        }

        User user = userRepo.findById(userId).orElseThrow(
            () -> new ResourceNotFoundException("User " + userId + " not found"));
        Category category = categoryRepo.findById(categoryId).orElseThrow(
            () -> new ResourceNotFoundException("Category " + categoryId + " not found"));

        Transaction t = new Transaction();
        t.setUser(user);
        t.setCategory(category);
        t.setAmount(amount);
        t.setTxnDate(date != null ? date : LocalDate.now());
        t.setDescription(description);
        t.setType(type);
        return txnRepo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        if (!txnRepo.existsById(id)) {
            throw new ResourceNotFoundException("Transaction " + id + " not found");
        }
        txnRepo.deleteById(id);
    }

    @Transactional
    public Transaction update(Long id, BigDecimal amount, LocalDate date,
                              String description, String type) {
        Transaction t = getById(id);
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidTransactionException("amount must be > 0");
            }
            t.setAmount(amount);
        }
        if (date != null) t.setTxnDate(date);
        if (description != null) t.setDescription(description);
        if (type != null) t.setType(type);
        return txnRepo.save(t);
    }
}
