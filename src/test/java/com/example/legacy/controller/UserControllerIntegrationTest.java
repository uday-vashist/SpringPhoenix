package com.example.legacy.controller;

import com.example.legacy.model.User;
import com.example.legacy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link UserController} using a full Spring Boot
 * context and MockMvc — no mocks anywhere in the stack.
 *
 * <p>Each test is wrapped in a transaction that rolls back on completion, so
 * the database is always in the state established by {@link BeforeEach}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController — Integration Tests (MockMvc)")
class UserControllerIntegrationTest {

    private static final String BASE_URL = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User persistedUser;

    @BeforeEach
    void setUp() {
        persistedUser = userRepository.save(new User("ctrl_alice", "ctrl_alice@it.com"));
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users returns 200 and list of all users")
    void getAllUsers_returns200WithUserList() throws Exception {
        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.username == 'ctrl_alice')]").exists());
    }

    @Test
    @DisplayName("GET /api/users returns 200 with empty array when no users exist")
    void getAllUsers_emptyDb_returns200WithEmptyArray() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/{id} returns 200 and the user JSON for a known ID")
    void getUserById_knownId_returns200WithUser() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + persistedUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(persistedUser.getId()))
                .andExpect(jsonPath("$.username").value("ctrl_alice"))
                .andExpect(jsonPath("$.email").value("ctrl_alice@it.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} returns 404 for an unknown ID")
    void getUserById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/users ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users returns 201 Created with saved user body")
    void createUser_validPayload_returns201WithBody() throws Exception {
        String json = objectMapper.writeValueAsString(new User("ctrl_bob", "ctrl_bob@it.com"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("ctrl_bob"))
                .andExpect(jsonPath("$.email").value("ctrl_bob@it.com"));
    }

    @Test
    @DisplayName("POST /api/users returns 400 Bad Request when username is blank")
    void createUser_blankUsername_returns400() throws Exception {
        String json = objectMapper.writeValueAsString(new User("", "valid@it.com"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users returns 400 Bad Request when email is blank")
    void createUser_blankEmail_returns400() throws Exception {
        String json = objectMapper.writeValueAsString(new User("validuser", ""));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users returns 400 Bad Request when email format is invalid")
    void createUser_invalidEmail_returns400() throws Exception {
        String json = "{\"username\":\"validuser\",\"email\":\"not-an-email\"}";

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users actually persists the new user in the database")
    void createUser_persistsToDatabase() throws Exception {
        String json = objectMapper.writeValueAsString(new User("ctrl_charlie", "charlie@it.com"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        assertThat(userRepository.findByUsername("ctrl_charlie")).isPresent();
    }

    // ─── PUT /api/users/{id} ──────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/users/{id} returns 200 OK with updated user for a known ID")
    void updateUser_knownId_returns200WithUpdatedBody() throws Exception {
        String json = objectMapper.writeValueAsString(
                new User("ctrl_alice_v2", "ctrl_alice_v2@it.com"));

        mockMvc.perform(put(BASE_URL + "/" + persistedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ctrl_alice_v2"))
                .andExpect(jsonPath("$.email").value("ctrl_alice_v2@it.com"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} returns 404 for an unknown ID")
    void updateUser_unknownId_returns404() throws Exception {
        String json = objectMapper.writeValueAsString(
                new User("ghost", "ghost@it.com"));

        mockMvc.perform(put(BASE_URL + "/" + Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{id} returns 400 when updated username is blank")
    void updateUser_blankUsername_returns400() throws Exception {
        String json = "{\"username\":\"\",\"email\":\"valid@it.com\"}";

        mockMvc.perform(put(BASE_URL + "/" + persistedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/{id} change is durable in the database")
    void updateUser_changeIsPersisted() throws Exception {
        String json = objectMapper.writeValueAsString(
                new User("ctrl_alice_durable", "durable@it.com"));

        mockMvc.perform(put(BASE_URL + "/" + persistedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        User reloaded = userRepository.findById(persistedUser.getId()).orElseThrow();
        assertThat(reloaded.getUsername()).isEqualTo("ctrl_alice_durable");
    }

    // ─── DELETE /api/users/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/users/{id} returns 204 No Content for a known ID")
    void deleteUser_knownId_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + persistedUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} actually removes the record from the database")
    void deleteUser_removesRecordFromDatabase() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + persistedUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(persistedUser.getId())).isEmpty();
    }
}
