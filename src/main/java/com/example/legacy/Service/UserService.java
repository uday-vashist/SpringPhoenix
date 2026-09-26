package com.example.legacy.service;

import com.example.legacy.model.User;
import com.example.legacy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link User} CRUD operations.
 *
 * <p>Modernization changes from legacy version:
 * <ul>
 *   <li>{@code @Transactional(readOnly = true)} on read methods — improves
 *       performance by disabling dirty checking and flushing on read paths</li>
 *   <li>{@code @Transactional} on write methods — ensures atomicity</li>
 *   <li>Added {@link #updateUser(Long, User)} — the legacy code had no update operation</li>
 * </ul>
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Updates the {@code username} and {@code email} of an existing user.
     *
     * @param id          the id of the user to update
     * @param userDetails object carrying the new field values
     * @return the updated user wrapped in an {@link Optional}, or empty if not found
     */
    @Transactional
    public Optional<User> updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(existing -> {
            existing.setUsername(userDetails.getUsername());
            existing.setEmail(userDetails.getEmail());
            return userRepository.save(existing);
        });
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
