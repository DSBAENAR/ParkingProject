package com.parking.core.service;

import java.util.List;

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
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a user by searching sequentially by name, username, or email.
     * <p>
     * The method attempts to find a user in the following order:
     * <ol>
     *   <li>By name</li>
     *   <li>By username</li>
     *   <li>By email</li>
     * </ol>
     * If no user is found with any of these identifiers, a {@link ResponseStatusException}
     * with {@code HttpStatus.BAD_REQUEST} is thrown.
     *
     * @param name the name of the user to search for
     * @param username the username of the user to search for
     * @param email the email of the user to search for
     * @return the found {@link User}
     * @throws ResponseStatusException if no user is found with the given identifiers
     */
    public User getUser(String name, String username, String email){
        return userRepository
                    .findByName(name)
                    .or(() -> userRepository.findByUsername(username))
                    .or(() -> userRepository.findByEmail(email))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User not found"));
    }

    /**
     * Retrieves all users from the repository.
     * 
     * @return a list of all users if the repository is not empty.
     * @throws ResponseStatusException if no users are found in the repository,
     *         with a status of BAD_REQUEST and a message indicating the absence of users.
     */
    public List<User> getAllUsers(){
        if (!userRepository.findAll().isEmpty()) return userRepository.findAll();
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "There are not users");
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param pageNumber the page number to retrieve (zero-based index).
     * @return a {@link PageResponse} containing the list of users for the requested page,
     *         the current page number, the total number of pages, and the total number of elements.
     */
    public PageResponse<User> getUsersPaginated(int pageNumber){

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("username"));
        Page<User> page = userRepository.findAll(pageable);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getTotalPages(),page.getTotalElements());
    }

    

}
