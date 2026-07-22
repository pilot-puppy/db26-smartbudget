package com.smartbudget.model;

import java.time.LocalDateTime;

// ============================================================
// TICKET-F012 (Day 2, Sprint 1) — User POJO
// ============================================================
public class User {

    private int userId;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(int userId, String name, String email, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public User(String name, String email) {
        this(0, name, email, LocalDateTime.now());
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{userId=" + userId
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
