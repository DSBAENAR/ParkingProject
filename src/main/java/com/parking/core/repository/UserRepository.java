package com.parking.core.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parking.core.model.User;
import com.parking.core.model.UserId;


@Repository
public interface UserRepository extends JpaRepository<User,UserId>{
    Optional<User> findByName(String name);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Page<User> findAll(Pageable pageable);
}
