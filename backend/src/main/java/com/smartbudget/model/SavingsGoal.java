package com.smartbudget.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// ============================================================
// TICKET-F015 (Day 2, Sprint 1) — SavingsGoal POJO
// ============================================================
public class SavingsGoal {

    private int goalId;
    private int userId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;

    public SavingsGoal() {
    }

    public SavingsGoal(int goalId, int userId, String goalName,
                       BigDecimal targetAmount, BigDecimal currentAmount,
                       LocalDate deadline) {
        this.goalId = goalId;
        this.userId = userId;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount
                .divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean isCompleted() {
        return currentAmount != null
                && targetAmount != null
                && currentAmount.compareTo(targetAmount) >= 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Goal[%s, %.2f / %.2f (%.1f%%), due %s]",
                goalName, currentAmount, targetAmount,
                getProgressPercentage(), deadline);
    }
}
