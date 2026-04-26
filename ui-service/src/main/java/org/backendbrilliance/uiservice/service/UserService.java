package org.backendbrilliance.uiservice.service;

import org.backendbrilliance.common.enums.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.exception.EmailAlreadyExistsException;
import org.backendbrilliance.uiservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User register(String email, String rawPassword, Tier tier) {
        if (existsByEmail(email)) {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email.toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .tier(null!=tier ? tier: Tier.FREE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user [id={}, email={}]", saved.getId(), saved.getEmail());
        return saved;
    }

    @Transactional
    public User register(String email, String rawPassword) {
        return register(email, rawPassword, Tier.FREE);
    }
}
