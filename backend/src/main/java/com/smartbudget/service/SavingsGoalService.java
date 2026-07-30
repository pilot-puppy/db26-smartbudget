package com.smartbudget.service;

import com.smartbudget.entity.SavingsGoal;
import com.smartbudget.entity.User;
import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.exception.ResourceNotFoundException;
import com.smartbudget.repository.SavingsGoalRepository;
import com.smartbudget.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ============================================================
// TICKET-F061/F062 (Day 6, Sprint 5) — Savings Goal Service
// ============================================================
//
// WHAT: This service handles business logic for savings goals.
//       After implementing, refactor SavingsGoalController to use this service
//       instead of calling the repository directly.
//
// WHY:  The controller should only handle HTTP concerns (request/response).
//       Business rules like "contribution must be > 0" belong in the service.
//       This separation makes your code testable — you can test service logic
//       WITHOUT starting a web server.
//
// ============================================================
@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepo;
    private final UserRepository userRepo;

    public SavingsGoalService(SavingsGoalRepository goalRepo, UserRepository userRepo) {
        this.goalRepo = goalRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> getByUserId(Long userId) {
        return goalRepo.findByUser_UserId(userId);
    }

    @Transactional(readOnly = true)
    public SavingsGoal getById(Long id) {
        return goalRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Savings goal " + id + " not found"));
    }

    @Transactional
    public SavingsGoal contribute(Long goalId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Contribution must be > 0");
        }
        SavingsGoal goal = getById(goalId);
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        return goalRepo.save(goal);
    }

    @Transactional
    public SavingsGoal create(Long userId, String name, BigDecimal target, LocalDate deadline) {
        User user = userRepo.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User " + userId + " not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setName(name);
        goal.setTargetAmount(target);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(deadline);
        return goalRepo.save(goal);
    }
}
