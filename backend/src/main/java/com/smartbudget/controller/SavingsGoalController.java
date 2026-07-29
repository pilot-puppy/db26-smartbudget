package com.smartbudget.controller;

import com.smartbudget.entity.SavingsGoal;
import com.smartbudget.service.SavingsGoalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ============================================================
// TICKET-F061/F062 (Day 6, Sprint 5) — Savings Goal REST Controller
// ============================================================
//
// WHAT: This controller exposes HTTP endpoints for savings goals.
//       A savings goal tracks progress toward a financial target
//       (e.g., "Save £5000 for holiday by December 2026").
//       The key feature is the "contribute" endpoint — adding money to a goal.
//
// WHY:  Savings goals are a core feature of any personal finance app.
//       This controller demonstrates a business operation (contribute)
//       beyond simple CRUD — it modifies an existing resource's state.
//
// REFERENCE: See CategoryController.java for the basic pattern.
// ============================================================
@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService service;

    public SavingsGoalController(SavingsGoalService service) {
        this.service = service;
    }

    // F061 Step 3 — GET /api/goals/user/{userId}
    @GetMapping("/user/{userId}")
    public List<SavingsGoal> byUser(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    // F061 Step 4 — POST /api/goals (create a new goal)
    public record CreateGoalRequest(Long userId, String name, BigDecimal targetAmount, LocalDate deadline) { }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoal create(@RequestBody CreateGoalRequest body) {
        return service.create(body.userId(), body.name(), body.targetAmount(), body.deadline());
    }

    // F062 Step 5 — PUT /api/goals/{id}/contribute
    public record ContributionRequest(BigDecimal amount) { }

    @PutMapping("/{id}/contribute")
    public SavingsGoal contribute(@PathVariable Long id, @RequestBody ContributionRequest body) {
        return service.contribute(id, body.amount());
    }
}
