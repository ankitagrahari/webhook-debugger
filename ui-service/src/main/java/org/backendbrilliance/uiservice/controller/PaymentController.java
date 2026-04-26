package org.backendbrilliance.uiservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.service.RazorpayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody Map<String, String> body) {
        String paymentId = body.get("paymentId");
        String tierStr = body.get("tier");
        String userId = body.get("userId");

        try {
            Tier tier = Tier.valueOf(tierStr);
            razorpayService.confirmUpgrade(UUID.fromString(userId), tier, paymentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Payment confirmation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}