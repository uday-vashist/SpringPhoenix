package com.example.legacy.service;

import com.example.legacy.model.User;
import com.example.legacy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("Alice", "alice@example.com");
        User user2 = new User("Bob", "bob@example.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(2);
    }

    @Test
    void testGetUserById() {
        User user = new User("Alice", "alice@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = userService.getUserById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("Alice");
    }

    @Test
    void testSaveUser() {
        User user = new User("Charlie", "charlie@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = userService.saveUser(user);
        assertThat(saved.getUsername()).isEqualTo("Charlie");
    }

    @Test
    void testUpdateUserFound() {
        User existing = new User("Alice", "alice@example.com");
        User details = new User("Alice", "alice.new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        Optional<User> result = userService.updateUser(1L, details);
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice.new@example.com");
        verify(userRepository).save(existing);
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(99L, new User("Ghost", "ghost@example.com"));
        assertThat(result).isEmpty();
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
}
