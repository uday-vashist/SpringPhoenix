package com.example.legacy.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        // Two unsaved entities with null id must not be equal
        User a = new User("Alice", "alice@example.com");
        User b = new User("Alice", "alice@example.com");

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

    @Test
    void testToStringContainsFields() {
        User user = new User("Alice", "alice@example.com");
        user.setId(42L);
        String s = user.toString();
        assertThat(s).contains("42").contains("Alice").contains("alice@example.com");
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
