package com.parking.core.auth.model.Response;

/**
 * Represents an authentication response containing user details.
 *
 * @param name     The full name of the user.
 * @param email    The email address of the user.
 * @param username The username of the user.
 */
public record AuthResponse(
    String name,
    String email,
    String username
) {}
