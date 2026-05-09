package org.backendbrilliance.uiservice.service.security;

import lombok.RequiredArgsConstructor;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HookSpyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getTier().name())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("No user found: " + email));
    }
}