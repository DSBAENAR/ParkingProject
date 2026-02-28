package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.model.Response.PageResponse;
import com.parking.core.model.User;
import com.parking.core.service.UserService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller for user management endpoints.
 * <p>
 * Base path: {@code /api/v1/parking/users}
 * </p>
 *
 * @see UserService
 */
@RestController
@RequestMapping("api/v1/parking/users")
public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Searches for a user by name, username, or email.
     *
     * @param name     the user's name (optional)
     * @param username the user's username (optional)
     * @param email    the user's email (optional)
     * @return {@code 200 OK} with the found user
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> findUser(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(Map.of("user", userService.getUser(name, username, email)));
    }

    /**
     * Retrieves all registered users.
     *
     * @return {@code 200 OK} with a list of all users
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(Map.of("users", userService.getAllUsers()));
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param pageNumber zero-based page index
     * @return {@code 200 OK} with paginated user data
     */
    @GetMapping("/pages")
    public ResponseEntity<PageResponse<User>> getPaginated(
            @RequestParam int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(userService.getUsersPaginated(pageNumber, pageSize));
    }
    
    
    
    
}
