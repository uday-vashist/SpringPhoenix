package com.example.legacy.controller;

import com.example.legacy.model.User;
import com.example.legacy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for {@link User} resources.
 *
 * <p>Modernization changes from legacy version:
 * <ul>
 *   <li>Added {@code @Valid} to request body parameters — activates Bean Validation
 *       constraints declared on {@link User} before the method body executes</li>
 *   <li>Added {@code PUT /api/users/{id}} update endpoint — the legacy API had no
 *       way to modify an existing user</li>
 *   <li>{@code POST} now returns HTTP 201 Created via
 *       {@link ResponseEntity} instead of a bare entity</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user and returns HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User saved = userService.saveUser(user);
        return ResponseEntity.status(201).body(saved);
    }

    /**
     * Updates an existing user's fields. Returns 200 OK with the updated user,
     * or 404 Not Found if no user exists for the given {@code id}.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
