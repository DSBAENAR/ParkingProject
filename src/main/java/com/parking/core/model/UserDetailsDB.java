package com.parking.core.model;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.parking.core.repository.UserRepository;

@Service
public class UserDetailsDB implements UserDetailsService{

    private final UserRepository userRepository;

    

    public UserDetailsDB(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    /**
     * Loads the user details by the provided username or email.
     *
     * This method retrieves a user from the database using the provided username
     * or email. If no user is found, a {@link UsernameNotFoundException} is thrown.
     * The retrieved user is then converted into a Spring Security {@link UserDetails}
     * object, which includes the username, password, and roles of the user.
     *
     * @param username the username or email of the user to be loaded
     * @return a {@link UserDetails} object containing the user's information
     * @throws UsernameNotFoundException if no user is found with the provided username or email
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
                User user = userRepository.findByUsernameOrEmail(username,username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
            return org.springframework.security.core.userdetails.User
                                                                    .builder()
                                                                    .username(user.getUsername())
                                                                    .password(user.getPassword())
                                                                    .roles(user.getRole().name())
                                                                    .build();
    }

}
