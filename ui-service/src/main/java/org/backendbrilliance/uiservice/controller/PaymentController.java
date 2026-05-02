package org.backendbrilliance.uiservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.common.enums.Tier;
import org.backendbrilliance.uiservice.service.RazorpayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    private final RazorpayService razorpayService;

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody Map<String, String> body) {
        String paymentId = body.get("paymentId");
        String orderId   = body.get("orderId");    // add this to JS callback
        String signature = body.get("signature"); // add this to JS callback
        String tierStr   = body.get("tier");
        String userId    = body.get("userId");

        // Verify before upgrading
        if (!verifySignature(orderId, paymentId, signature)) {
            log.warn("Invalid payment signature for userId={}", userId);
            return ResponseEntity.status(403).build();
        }

        try {
            Tier tier = Tier.valueOf(tierStr);
            razorpayService.confirmUpgrade(UUID.fromString(userId), tier, paymentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Payment confirmation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    razorpaySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256"));
            byte[] hash = mac.doFinal(
                    payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String computed = bytesToHex(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}