package com.example.legacy.repository;

import com.example.legacy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link UserRepository} running against a real H2
 * in-memory database.  Uses {@link DataJpaTest} which:
 * <ul>
 *   <li>Bootstraps only the JPA slice (no web layer)</li>
 *   <li>Wraps every test in a transaction that is rolled back on completion,
 *       so tests are fully isolated without manual teardown</li>
 * </ul>
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository — Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = entityManager.persistAndFlush(new User("alice_it", "alice@it.com"));
        bob   = entityManager.persistAndFlush(new User("bob_it",   "bob@it.com"));
    }

    // ─── findByUsername ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByUsername returns user when username exists")
    void findByUsername_existingUser_returnsUser() {
        Optional<User> result = userRepository.findByUsername("alice_it");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@it.com");
        assertThat(result.get().getId()).isEqualTo(alice.getId());
    }

    @Test
    @DisplayName("findByUsername returns empty for unknown username")
    void findByUsername_unknownUsername_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername("does_not_exist");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername is case-sensitive")
    void findByUsername_wrongCase_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername("ALICE_IT");

        // H2 in default mode uses case-sensitive LIKE/= on VARCHAR — asserts
        // that the repository does NOT silently normalise case.
        assertThat(result).isEmpty();
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll returns all persisted users")
    void findAll_returnsAllUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("alice_it", "bob_it");
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById returns user for a known ID")
    void findById_knownId_returnsUser() {
        Optional<User> result = userRepository.findById(alice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice_it");
    }

    @Test
    @DisplayName("findById returns empty for an unknown ID")
    void findById_unknownId_returnsEmpty() {
        Optional<User> result = userRepository.findById(Long.MAX_VALUE);

        assertThat(result).isEmpty();
    }

    // ─── save (insert + update) ───────────────────────────────────────────────

    @Test
    @DisplayName("save persists a new user and auto-generates its ID")
    void save_newUser_persistsAndAssignsId() {
        User charlie = userRepository.save(new User("charlie_it", "charlie@it.com"));

        assertThat(charlie.getId()).isNotNull().isPositive();
        assertThat(userRepository.findByUsername("charlie_it")).isPresent();
    }

    @Test
    @DisplayName("save on an existing user updates its fields")
    void save_existingUser_updatesFields() {
        alice.setEmail("alice.updated@it.com");
        userRepository.save(alice);
        entityManager.flush();
        entityManager.clear();   // evict L1 cache so next read hits the DB

        User reloaded = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(reloaded.getEmail()).isEqualTo("alice.updated@it.com");
    }

    @Test
    @DisplayName("save rejects a duplicate username (unique constraint)")
    void save_duplicateUsername_throwsDataIntegrityViolation() {
        assertThatThrownBy(() -> {
            userRepository.saveAndFlush(new User("alice_it", "other@it.com"));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ─── deleteById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById removes the user from the database")
    void deleteById_existingUser_removesRecord() {
        userRepository.deleteById(alice.getId());
        entityManager.flush();

        assertThat(userRepository.findById(alice.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById on an unknown ID does not throw")
    void deleteById_unknownId_noException() {
        // Spring Data JPA's deleteById throws EmptyResultDataAccessException if not found
        // before Spring Data 3.x; from 3.x it silently ignores missing IDs.
        // We document the actual behaviour here so any future contract change is caught.
        userRepository.deleteById(Long.MAX_VALUE);
        // No exception → test passes
    }

    // ─── count ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("count reflects the number of persisted users")
    void count_reflectsPersistedUsers() {
        assertThat(userRepository.count()).isEqualTo(2L);
    }

    @Test
    @DisplayName("count decrements after a delete")
    void count_decrementsAfterDelete() {
        userRepository.deleteById(alice.getId());
        entityManager.flush();

        assertThat(userRepository.count()).isEqualTo(1L);
    }
}
