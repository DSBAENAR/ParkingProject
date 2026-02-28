package com.parking.core.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.enums.Roles;
import com.parking.core.model.User;
import com.parking.core.model.Response.PageResponse;
import com.parking.core.service.UserService;

@WebMvcTest(UserHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("GET /user - should return user by name")
    void shouldReturnUserByName() throws Exception {
        User user = new User("John Doe", "johndoe", Roles.USER, "john@example.com", null);
        when(userService.getUser("John Doe", null, null)).thenReturn(user);

        mockMvc.perform(get("/api/v1/parking/users/user")
                        .param("name", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("johndoe"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /user - should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUser(any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/v1/parking/users/user")
                        .param("name", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET / - should return all users")
    void shouldReturnAllUsers() throws Exception {
        User u1 = new User("John", "johndoe", Roles.USER, "john@example.com", null);
        User u2 = new User("Jane", "janedoe", Roles.ADMIN, "jane@example.com", null);
        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/v1/parking/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(2));
    }

    @Test
    @DisplayName("GET /pages - should return paginated users")
    void shouldReturnPaginatedUsers() throws Exception {
        User user = new User("John", "johndoe", Roles.USER, "john@example.com", null);
        PageResponse<User> pageResponse = new PageResponse<>(List.of(user), 0, 1, 1);
        when(userService.getUsersPaginated(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/parking/users/pages")
                        .param("pageNumber", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
}
