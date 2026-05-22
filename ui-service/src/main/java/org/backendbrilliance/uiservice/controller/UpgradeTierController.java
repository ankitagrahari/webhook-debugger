package org.backendbrilliance.uiservice.controller;

import org.backendbrilliance.uiservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/")
public class UpgradeTierController {

    private final UserService userService;

    public UpgradeTierController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upgrade")
    public ResponseEntity<?> manualUpgrade(
            @RequestBody Map<String, String> body,
            @RequestHeader("X-Admin-Secret") String secret) {

        if (!secret.equals(System.getenv("ADMIN_SECRET"))) {
            return ResponseEntity.status(403).build();
        }

        userService.upgradeTier(body.get("email"), body.get("tier"));
        return ResponseEntity.ok(Map.of("status", "upgraded"));
    }
}
