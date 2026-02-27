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

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String name, String username, String email) {
        return userRepository
                .findByName(name)
                .or(() -> userRepository.findByUsername(username))
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found");
        }
        log.info("Retrieved {} users", users.size());
        return users;
    }

    public PageResponse<User> getUsersPaginated(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("username"));
        Page<User> page = userRepository.findAll(pageable);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getTotalPages(), page.getTotalElements());
    }
}
