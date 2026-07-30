package com.smartbudget.controller;

// ============================================================
// TICKET-F056 to F059 (Day 6, Sprint 5) — Transaction REST Controller.
// Self-contained: talks to the repositories directly (like CategoryController)
// and uses ResponseEntity for status codes.
// ============================================================

import com.smartbudget.entity.Category;
import com.smartbudget.entity.Transaction;
import com.smartbudget.entity.User;
import com.smartbudget.repository.CategoryRepository;
import com.smartbudget.repository.TransactionRepository;
import com.smartbudget.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository txnRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public TransactionController(TransactionRepository txnRepo,
                                 UserRepository userRepo,
                                 CategoryRepository categoryRepo) {
        this.txnRepo = txnRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    // F056 — GET /api/transactions
    @GetMapping
    public List<Transaction> getAll() {
        return txnRepo.findAll();
    }

    // F057 — POST /api/transactions   (@Valid enforces amount > 0 -> 400)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Transaction t) {
        Long userId = t.getUser() != null ? t.getUser().getUserId() : null;
        Long categoryId = t.getCategory() != null ? t.getCategory().getCategoryId() : null;
        Optional<User> user = userId == null ? Optional.empty() : userRepo.findById(userId);
        Optional<Category> category = categoryId == null ? Optional.empty() : categoryRepo.findById(categoryId);
        if (user.isEmpty() || category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User or Category not found"));
        }
        t.setUser(user.get());
        t.setCategory(category.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(txnRepo.save(t));
    }

    // F058 — GET /api/transactions/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(@PathVariable Long userId) {
        return txnRepo.findByUser_UserIdOrderByTxnDateDesc(userId);
    }

    // F059 — DELETE /api/transactions/{id}   (204, or 404 if missing)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!txnRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        txnRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
