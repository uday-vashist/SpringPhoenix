package com.example.legacy.controller;

import com.example.legacy.model.User;
import com.example.legacy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User("Alice", "alice@example.com");
        User user2 = new User("Bob", "bob@example.com");
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userController.getAllUsers();
        assertThat(users).hasSize(2);
    }

    @Test
    void testGetUserByIdFound() {
        User user = new User("Alice", "alice@example.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById(1L);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getUsername()).isEqualTo("Alice");
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserById(1L);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testCreateUser() {
        User user = new User("Charlie", "charlie@example.com");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(user);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().getUsername()).isEqualTo("Charlie");
    }

    @Test
    void testUpdateUserFound() {
        User updated = new User("Charlie", "charlie.new@example.com");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(Optional.of(updated));

        ResponseEntity<User> response = userController.updateUser(1L, updated);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getEmail()).isEqualTo("charlie.new@example.com");
    }

    @Test
    void testUpdateUserNotFound() {
        User details = new User("Ghost", "ghost@example.com");
        when(userService.updateUser(eq(99L), any(User.class))).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.updateUser(99L, details);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}
