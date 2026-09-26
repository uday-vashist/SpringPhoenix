package com.example.legacy.model;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link User} — covers equals/hashCode/toString and
 * the validation-annotated fields.
 */
class UserTest {

    @Test
    void testEqualsWhenSameId() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        User b = new User("Different", "other@example.com");
        b.setId(1L);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void testEqualsWhenDifferentId() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        User b = new User("Alice", "alice@example.com");
        b.setId(2L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testEqualsWhenTransient() {
        // Two unsaved entities with null id must not be equal — and must not throw NPE.
        // PITest's RemoveConditionalMutator removes the `id != null` guard, turning
        // `id != null && id.equals(other.id)` into `id.equals(other.id)`, which throws
        // NullPointerException for transient entities. AssertJ's isNotEqualTo swallows
        // NPE and still passes, so we explicitly assert no exception is thrown first.
        User a = new User("Alice", "alice@example.com");
        User b = new User("Alice", "alice@example.com");

        assertThatCode(() -> a.equals(b)).doesNotThrowAnyException();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testEqualsSameInstance() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        assertThat(a).isEqualTo(a);
    }

    @Test
    void testEqualsNullAndDifferentType() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("some string");
    }

    @Test
    void testHashCodeConsistency() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        assertThat(a.hashCode()).isEqualTo(a.hashCode());
    }

    /**
     * Kills the PrimitiveReturnsMutator on hashCode() line 79.
     *
     * <p>PITest mutates {@code return Objects.hashCode(id)} → {@code return 0}.
     * Asserting the exact value against {@code Objects.hashCode(id)} means the
     * mutant produces {@code 0} while the assertion expects the real hash, causing
     * a test failure that kills the mutant.</p>
     *
     * <p>The two-entity inequality check reinforces that the hash is driven by the
     * id value, not a constant — catching any constant-substitution variant.</p>
     */
    @Test
    void testHashCodeMatchesObjectsHashCodeOfId() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);

        // Must equal Objects.hashCode(id) — kills "replaced int return with 0"
        assertThat(a.hashCode()).isEqualTo(Objects.hashCode(1L));
    }

    @Test
    void testHashCodeDiffersForDifferentIds() {
        User a = new User("Alice", "alice@example.com");
        a.setId(1L);
        User b = new User("Alice", "alice@example.com");
        b.setId(2L);

        // Two users with distinct ids must produce distinct hash codes,
        // proving the result is id-driven and not a constant.
        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    @Test
    void testHashCodeForTransientUserIsZero() {
        // A transient user (id == null) hashes to Objects.hashCode(null) == 0.
        // Asserting the concrete value prevents the mutant from "accidentally" surviving
        // because returning 0 would match the expected value for null ids.
        User transient1 = new User("Alice", "alice@example.com");

        assertThat(transient1.hashCode()).isEqualTo(Objects.hashCode(null));
    }

    @Test
    void testToStringContainsFields() {
        User user = new User("Alice", "alice@example.com");
        user.setId(42L);
        String s = user.toString();
        // Pin the exact field labels so a label-swap mutant (e.g. "username" ↔ "email")
        // is caught — checking raw values alone would not kill that mutant.
        assertThat(s)
                .contains("id=42")
                .contains("username='Alice'")
                .contains("email='alice@example.com'");
    }

    @Test
    void testConvenienceConstructorAssignsFields() {
        // Kills VOID_METHOD_CALL / NullReturnsMutator on the 2-arg constructor body.
        // Other tests use the constructor but only assert on equality/hashCode;
        // a mutation that nulls out assignments would still pass those tests.
        User user = new User("Charlie", "charlie@example.com");

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("Charlie");
        assertThat(user.getEmail()).isEqualTo("charlie@example.com");
    }

    @Test
    void testDefaultConstructorAndSetters() {
        User user = new User();
        user.setId(5L);
        user.setUsername("Bob");
        user.setEmail("bob@example.com");

        assertThat(user.getId()).isEqualTo(5L);
        assertThat(user.getUsername()).isEqualTo("Bob");
        assertThat(user.getEmail()).isEqualTo("bob@example.com");
    }
}
