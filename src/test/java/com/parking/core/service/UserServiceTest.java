package com.parking.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.enums.Roles;
import com.parking.core.model.User;
import com.parking.core.model.Response.PageResponse;
import com.parking.core.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "johndoe", Roles.USER, "john@example.com", null);
    }

    @Nested
    @DisplayName("getUser")
    class GetUserTests {

        @Test
        @DisplayName("should return user found by name")
        void shouldFindByName() {
            when(userRepository.findByName("John Doe")).thenReturn(Optional.of(testUser));

            User result = userService.getUser("John Doe", null, null);

            assertEquals("johndoe", result.getUsername());
            verify(userRepository).findByName("John Doe");
        }

        @Test
        @DisplayName("should return user found by username when name not found")
        void shouldFindByUsername() {
            when(userRepository.findByName("Unknown")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

            User result = userService.getUser("Unknown", "johndoe", null);

            assertEquals("john@example.com", result.getEmail());
        }

        @Test
        @DisplayName("should return user found by email when name and username not found")
        void shouldFindByEmail() {
            when(userRepository.findByName(any())).thenReturn(Optional.empty());
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

            User result = userService.getUser("x", "y", "john@example.com");

            assertEquals("John Doe", result.getName());
        }

        @Test
        @DisplayName("should throw 404 when user not found by any field")
        void shouldThrow404WhenNotFound() {
            when(userRepository.findByName(any())).thenReturn(Optional.empty());
            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.getUser("x", "y", "z"));

            assertEquals(404, ex.getStatusCode().value());
            assertEquals("User not found", ex.getReason());
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            User user2 = new User("Jane", "janedoe", Roles.ADMIN, "jane@example.com", null);
            when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

            List<User> result = userService.getAllUsers();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should throw 404 when no users exist")
        void shouldThrow404WhenEmpty() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.getAllUsers());

            assertEquals(404, ex.getStatusCode().value());
            assertEquals("No users found", ex.getReason());
        }
    }

    @Nested
    @DisplayName("getUsersPaginated")
    class GetUsersPaginatedTests {

        @Test
        @DisplayName("should return paginated response")
        void shouldReturnPageResponse() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
            Page<User> page = new PageImpl<>(List.of(testUser), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(page);

            PageResponse<User> response = userService.getUsersPaginated(0, 10);

            assertEquals(1, response.content().size());
            assertEquals(0, response.currentPage());
            assertEquals(1, response.totalPages());
            assertEquals(1, response.total());
        }

        @Test
        @DisplayName("should return empty page when no users")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));
            Page<User> page = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(userRepository.findAll(pageable)).thenReturn(page);

            PageResponse<User> response = userService.getUsersPaginated(0, 10);

            assertTrue(response.content().isEmpty());
            assertEquals(0, response.total());
        }
    }
}
