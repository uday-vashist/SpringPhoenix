package com.example.legacy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * JPA entity representing an application user.
 *
 * <p>Modernization changes from legacy version:
 * <ul>
 *   <li>Added Bean Validation constraints ({@code @NotBlank}, {@code @Email})</li>
 *   <li>Added {@code @Column} constraints (nullable=false, unique on username)</li>
 *   <li>Implemented {@code equals}, {@code hashCode}, and {@code toString}
 *       based on the surrogate {@code id} field, which is the JPA best-practice</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username must not be blank")
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Column(name = "email", nullable = false)
    private String email;

    /** Required no-arg constructor for JPA and Jackson deserialization. */
    public User() {
    }

    /** Convenience constructor for creating new users before persistence. */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // -----------------------------------------------------------------------
    // Getters and Setters
    // -----------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // -----------------------------------------------------------------------
    // equals / hashCode / toString
    // -----------------------------------------------------------------------

    /**
     * Equality is based on {@code id} only (surrogate key comparison).
     * Two transient entities (id == null) are never equal to each other.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Use a constant so transient entities hash consistently within a session.
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
