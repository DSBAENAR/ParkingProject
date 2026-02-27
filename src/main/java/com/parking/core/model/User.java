package com.parking.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parking.core.enums.Roles;
import com.parking.core.payment.Requests.UserAddress;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@IdClass(UserId.class)
@Entity
@Table(name = "users")
public class User {

    @NotBlank(message = "Name is required")
    private String name;

    @Id
    @NotBlank(message = "Username is required")
    private String username;

    @Id
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @JsonIgnore
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
