package com.parking.core.model.dto;

import com.parking.core.enums.Roles;

/**
 * Data Transfer Object for {@link com.parking.core.model.User}.
 * Excludes sensitive fields such as password.
 *
 * @param name     the user's full name
 * @param username the unique username
 * @param email    the user's email address
 * @param role     the user's assigned role
 */
public record UserDTO(
    String name,
    String username,
    String email,
    Roles role
) {}
