package com.parking.core.model;

import com.parking.core.enums.Roles;
import com.parking.core.payment.Requests.UserAddress;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@IdClass(UserId.class)
@Entity
@Table(
    name = "users"
)
public class User {

    private String name;

    @Id
    private String username;

    @Id
    private String email;

    @Enumerated(EnumType.STRING)
    private Roles role;

    private String password;

    @Embedded
    private UserAddress address;

    public User(){}

    public User(String name, String username, Roles role, String email, UserAddress userAddress){
        this.name = name;
        this.username = username;
        this.role = role;
        this.email = email;
        this.address = userAddress;
    }

    public String getName() {
        return name;
    }
    
    public String getUsername() {
        return username;
    }

    public Roles getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }
    
}
