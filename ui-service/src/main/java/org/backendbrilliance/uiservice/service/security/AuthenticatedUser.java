package org.backendbrilliance.uiservice.service.security;

import lombok.RequiredArgsConstructor;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper bean to get the currently logged-in User entity from the DB.
 * Inject this anywhere you need the current user.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final UserRepository userRepository;

    public Optional<User> get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String email = authentication.getName();
        if (email == null || email.equals("anonymousUser")) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email);
    }
}