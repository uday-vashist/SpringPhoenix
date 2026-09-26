package com.example.legacy.repository;

import com.example.legacy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        User user = new User("Bob", "bob@example.com");
        userRepository.save(user);

        User found = userRepository.findByUsername("Bob").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> found = userRepository.findByUsername("NonExistentUser");
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User user = new User("Alice", "alice@example.com");
        userRepository.save(user);

        User savedUser = userRepository.findByUsername("Alice").get();
        savedUser.setEmail("alice.new@example.com");
        userRepository.save(savedUser);

        User updatedUser = userRepository.findByUsername("Alice").get();
        assertThat(updatedUser.getEmail()).isEqualTo("alice.new@example.com");
    }

    @Test
    void testDeleteUser() {
        User user = new User("Charlie", "charlie@example.com");
        userRepository.save(user);

        Long userId = userRepository.findByUsername("Charlie").get().getId();
        userRepository.deleteById(userId);

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void testFindAllUsers() {
        userRepository.save(new User("UserOne", "one@example.com"));
        userRepository.save(new User("UserTwo", "two@example.com"));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }
}