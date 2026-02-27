package com.parking.core.model.dto;

import com.parking.core.enums.Roles;

public record UserDTO(
    String name,
    String username,
    String email,
    Roles role
) {}
