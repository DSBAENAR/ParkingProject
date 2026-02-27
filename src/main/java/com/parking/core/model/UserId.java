package com.parking.core.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for the {@link User} entity.
 * <p>
 * Combines {@code username} and {@code email} to form the compound key.
 * Implements {@link Serializable} as required by JPA for composite keys.
 * </p>
 *
 * @see User
 */
public class UserId implements Serializable{
    private String username;
    private String email;

    public UserId(){}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (!(obj instanceof UserId)) return false;
        UserId other = (UserId) obj;

        return Objects.equals(username, other.username) && 
                Objects.equals(email, other.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username,email);
    }
}