package com.example.legacy.service;

import com.example.legacy.model.User;
import com.example.legacy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserService} using a real Spring context and
 * an H2 in-memory database — no mocks.
 *
 * <p>Each test runs inside a transaction that is rolled back on completion
 * ({@code @Transactional} on the class), so every test starts with a clean
 * database state produced by {@link BeforeEach}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService — Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User persisted;

    @BeforeEach
    void setUp() {
        // Each test starts with exactly one user in the DB.
        persisted = userService.saveUser(new User("svc_alice", "svc_alice@it.com"));
    }

    // ─── getAllUsers ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers returns all users currently in the database")
    void getAllUsers_returnsAllUsers() {
        userService.saveUser(new User("svc_bob", "svc_bob@it.com"));

        List<User> users = userService.getAllUsers();

        assertThat(users)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(User::getUsername)
                .contains("svc_alice", "svc_bob");
    }

    @Test
    @DisplayName("getAllUsers returns empty list when no users exist")
    void getAllUsers_emptyDatabase_returnsEmptyList() {
        userRepository.deleteAll();

        List<User> users = userService.getAllUsers();

        assertThat(users).isEmpty();
    }

    // ─── getUserById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById returns user for a known ID")
    void getUserById_knownId_returnsUser() {
        Optional<User> result = userService.getUserById(persisted.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("svc_alice");
        assertThat(result.get().getEmail()).isEqualTo("svc_alice@it.com");
    }

    @Test
    @DisplayName("getUserById returns empty Optional for an unknown ID")
    void getUserById_unknownId_returnsEmpty() {
        Optional<User> result = userService.getUserById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    // ─── saveUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveUser persists user and assigns a generated ID")
    void saveUser_newUser_persistsWithId() {
        User charlie = userService.saveUser(new User("svc_charlie", "svc_charlie@it.com"));

        assertThat(charlie.getId()).isNotNull().isPositive();
        assertThat(userRepository.findByUsername("svc_charlie")).isPresent();
    }

    @Test
    @DisplayName("saveUser sets both username and email correctly")
    void saveUser_fieldsArePersistedCorrectly() {
        User saved = userService.saveUser(new User("svc_diana", "diana@it.com"));

        assertThat(saved.getUsername()).isEqualTo("svc_diana");
        assertThat(saved.getEmail()).isEqualTo("diana@it.com");
    }

    // ─── updateUser ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser modifies username and email of an existing user")
    void updateUser_existingId_updatesFields() {
        User details = new User("svc_alice_v2", "svc_alice_v2@it.com");

        Optional<User> result = userService.updateUser(persisted.getId(), details);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("svc_alice_v2");
        assertThat(result.get().getEmail()).isEqualTo("svc_alice_v2@it.com");
    }

    @Test
    @DisplayName("updateUser change is visible after the service call (DB round-trip)")
    void updateUser_changesArePersisted() {
        userService.updateUser(persisted.getId(), new User("svc_alice_v3", "v3@it.com"));

        // Re-read directly from repository to confirm persistence
        User reloaded = userRepository.findById(persisted.getId()).orElseThrow();
        assertThat(reloaded.getUsername()).isEqualTo("svc_alice_v3");
        assertThat(reloaded.getEmail()).isEqualTo("v3@it.com");
    }

    @Test
    @DisplayName("updateUser returns empty Optional when user ID does not exist")
    void updateUser_unknownId_returnsEmpty() {
        Optional<User> result = userService.updateUser(Long.MAX_VALUE,
                new User("ghost", "ghost@it.com"));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateUser does not create a new record for an unknown ID")
    void updateUser_unknownId_doesNotCreateRecord() {
        long before = userRepository.count();

        userService.updateUser(Long.MAX_VALUE, new User("phantom", "phantom@it.com"));

        assertThat(userRepository.count()).isEqualTo(before);
    }

    // ─── deleteUser ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser removes the user from the database")
    void deleteUser_existingId_removesRecord() {
        userService.deleteUser(persisted.getId());

        assertThat(userRepository.findById(persisted.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteUser decrements total user count")
    void deleteUser_decrementsCount() {
        long before = userRepository.count();

        userService.deleteUser(persisted.getId());

        assertThat(userRepository.count()).isEqualTo(before - 1);
    }
}
