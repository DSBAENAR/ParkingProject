package com.parking.core.auth.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.parking.core.auth.services.JWTAuthFilter;
import com.parking.core.model.UserDetailsDB;

@Configuration
public class SecurityConfig {
    private final JWTAuthFilter jwtAuthFilter;
    private final UserDetailsDB userDetailsDB;

    public SecurityConfig(JWTAuthFilter jwtAuthFilter,UserDetailsDB userDetailsDB) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsDB = userDetailsDB;
    }

    /**
     * Creates and provides a bean of type {@link PasswordEncoder} using the 
     * {@link BCryptPasswordEncoder} implementation. This bean is used to 
     * securely encode passwords for storage and comparison.
     *
     * @return an instance of {@link BCryptPasswordEncoder} for password encoding.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new  BCryptPasswordEncoder();
    }

    /**
     * Configures and provides an {@link AuthenticationProvider} bean for authentication.
     * This method sets up a {@link DaoAuthenticationProvider} with a custom user details
     * service and a password encoder.
     *
     * @param encoder the {@link PasswordEncoder} to be used for encoding and verifying passwords.
     * @return an {@link AuthenticationProvider} configured with the provided password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder){
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsDB);
        auth.setPasswordEncoder(encoder);
        return auth;    
    }

    /**
     * Creates and provides an instance of {@link AuthenticationManager} using the provided
     * {@link AuthenticationConfiguration}.
     *
     * @param conf the {@link AuthenticationConfiguration} used to configure and retrieve the
     *             {@link AuthenticationManager}.
     * @return an instance of {@link AuthenticationManager}.
     * @throws Exception if an error occurs while retrieving the {@link AuthenticationManager}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration conf) throws Exception{
        return conf.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain for the application.
     *
     * <p>This method sets up the following security configurations:
     * <ul>
     *   <li>Disables CSRF protection as it is not required for stateless APIs.</li>
     *   <li>Configures session management to be stateless, ensuring no session is created or used.</li>
     *   <li>Defines authorization rules:
     *       <ul>
     *           <li>Allows unrestricted access to endpoints under "/api/v1/parking/auth/**".</li>
     *           <li>Restricts access to endpoints under "/api/v1/parking/users/**" to users with the "USER" role.</li>
     *           <li>Requires authentication for all other requests.</li>
     *       </ul>
     *   </li>
     *   <li>Sets up a custom authentication provider with a password encoder.</li>
     *   <li>Adds a JWT authentication filter before the UsernamePasswordAuthenticationFilter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object to configure the security settings
     * @return the configured {@link SecurityFilterChain} object
     * @throws Exception if an error occurs while configuring the security settings
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csfr -> csfr.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                                            .requestMatchers("/api/v1/parking/auth/**").permitAll()
                                            .requestMatchers("/h2-console/**").permitAll()
                                            .requestMatchers("/api/v1/parking/users/**").hasRole("USER")
                                            .requestMatchers("/api/customers/**").permitAll()
                                            .requestMatchers("/api/invoices/**").permitAll()
                                            .requestMatchers("/api/cards/**").permitAll()
                                            .anyRequest().authenticated())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authenticationProvider(authenticationProvider(passwordEncoder()))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    
}
