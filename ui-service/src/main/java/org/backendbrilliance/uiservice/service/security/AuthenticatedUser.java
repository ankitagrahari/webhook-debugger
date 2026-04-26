package org.backendbrilliance.uiservice.service.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper bean to get the currently logged-in User entity from the DB.
 * Inject this anywhere you need the current user.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public Optional<User> get() {
        return authenticationContext
                .getAuthenticatedUser(
                        org.springframework.security.core.userdetails.UserDetails.class)
                .flatMap(u -> userRepository.findByEmail(u.getUsername()));
    }

    public boolean isLoggedIn() {
        return authenticationContext.isAuthenticated();
    }

    /**
     * Logs out the current user and redirects to login page.
     * Uses Vaadin's AuthenticationContext which handles session invalidation.
     */
    public void logout() {
        authenticationContext.logout();
    }
}