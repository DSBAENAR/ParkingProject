package com.parking.core.handlers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.core.model.Response.PageResponse;
import com.parking.core.model.User;
import com.parking.core.service.UserService;

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
    public ResponseEntity<Map<String, Object>> findUser(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(Map.of("user", userService.getUser(name, username, email)));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(Map.of("users", userService.getAllUsers()));
    }

    @GetMapping("/pages")
    public ResponseEntity<PageResponse<User>> getPaginated(@RequestParam int pageNumber) {
        return ResponseEntity.ok(userService.getUsersPaginated(pageNumber));
    }
    
    
    
    
}
