package com.parking.core.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.model.User;
import com.parking.core.model.Response.PageResponse;
import com.parking.core.repository.UserRepository;

/**
 * Service layer for user management operations.
 * <p>
 * Provides methods to retrieve users by different criteria (name, username, email),
 * list all users, and support paginated queries.
 * </p>
 *
 * @see User
 * @see UserRepository
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Searches for a user by name, username, or email using a fallback chain.
     * <p>
     * The search is performed in the following order:
     * <ol>
     *   <li>By name</li>
     *   <li>By username (if not found by name)</li>
     *   <li>By email (if not found by username)</li>
     * </ol>
     * </p>
     *
     * @param name     the user's full name (optional)
     * @param username the user's username (optional)
     * @param email    the user's email address (optional)
     * @return the found {@link User}
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if no user matches any criteria
     */
    public User getUser(String name, String username, String email) {
        return userRepository
                .findByName(name)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Retrieves all registered users.
     *
     * @return a list of all {@link User} entities
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if no users exist
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found");
        }
        log.info("Retrieved {} users", users.size());
        return users;
    }

    /**
     * Retrieves a paginated list of users sorted by username.
     *
     * @param pageNumber zero-based page index
     * @return a {@link PageResponse} containing the users, current page, total pages, and total count
     */
    public PageResponse<User> getUsersPaginated(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("username"));
        Page<User> page = userRepository.findAll(pageable);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }
}
