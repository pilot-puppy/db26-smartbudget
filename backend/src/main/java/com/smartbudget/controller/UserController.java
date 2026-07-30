package com.smartbudget.controller;


// ============================================================
// TICKET-F060 (Day 6, Sprint 5) — User REST Controller.
// Self-contained CRUD over UserRepository (mirrors CategoryController).
// ============================================================

import com.smartbudget.entity.User;
import com.smartbudget.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // GET /api/users
    @GetMapping
    public List<User> getAll() {
        return repo.findAll();
    }

    // POST /api/users   (@Valid enforces @NotBlank name / @Email -> 400)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return repo.save(user);
    }

    // GET /api/users/{id}   (404 if missing)
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
