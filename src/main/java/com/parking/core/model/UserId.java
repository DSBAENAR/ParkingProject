package com.parking.core.model;

import java.io.Serializable;
import java.util.Objects;

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