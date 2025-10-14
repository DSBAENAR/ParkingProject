package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.service.UserService;


import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/v1/parking/users")
public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<?> findUser(
        @RequestParam(required = false) String name, 
        @RequestParam(required = false) String username, 
        @RequestParam(required = false) String email) {

            try {
                return ResponseEntity.ok(Collections.singletonMap("user", userService.getUser(name, username, email)));
            } catch (ResponseStatusException e) {
                return ResponseEntity
                                .status(e.getStatusCode())
                                .body(Collections.singletonMap("message", e.getReason()));
            }
    }

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        try {

            return ResponseEntity.ok(Map.of(
                "users", userService.getAllUsers()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity
                            .status(e.getStatusCode())
                            .body(Collections.singletonMap("message", e.getReason()));
        }
    }
    
    @GetMapping("/pages")
    public ResponseEntity<?> getMethodName(@RequestParam int pageNumber) {
        return ResponseEntity.ok(userService.getUsersPaginated(pageNumber));
    }
    
    
    
    
}
