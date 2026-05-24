package org.backendbrilliance.uiservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.dtos.LoginRequest;
import org.backendbrilliance.uiservice.dtos.RegisterRequest;
import org.backendbrilliance.uiservice.dtos.UserResponse;
import org.backendbrilliance.uiservice.service.UserService;
import org.backendbrilliance.uiservice.service.helper.MapperForEntityToDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final MapperForEntityToDTO mapper;

    public AuthController(
            AuthenticationManager authManager,
            UserService userService, MapperForEntityToDTO mapper) {
        this.authManager = authManager;
        this.userService = userService;
        this.mapper = mapper;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {

        var token = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        var auth = authManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // create/update session
        var session = httpReq.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        var user = userService.findByEmail(req.email());
        return user
                .map(value -> ResponseEntity.ok(mapper.userEntityToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        log.info("registering user {}", req);
        //Register user always with free tier
        var user = userService.register(req.email(), req.password());
        log.info("user registered: {}", user);
        return ResponseEntity.status(201).body(mapper.userEntityToDTO(user));
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        var email = Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getName();
        var user = userService.findByEmail(email);
        return user
                .map(value -> ResponseEntity.ok(mapper.userEntityToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
