package org.backendbrilliance.uiservice.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.config.RazorpayConfig;
import org.backendbrilliance.uiservice.dtos.APIError;
import org.backendbrilliance.uiservice.service.RazorpayService;
import org.backendbrilliance.uiservice.service.UserService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(path = "/api/payment", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final UserService userService;
    private final RazorpayService razorpayService;
    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    public PaymentController(UserService userService, RazorpayService razorpayService,
                             RazorpayClient razorpayClient, RazorpayConfig razorpayConfig) {
        this.userService = userService;
        this.razorpayService = razorpayService;
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
    }

    // ── POST /api/payment/order ────────────────────────────────────────────────
    // Creates a Razorpay order and returns orderId + keyId to the frontend
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            String tier = (String) body.get("tier");
            int amount = (Integer) body.get("amount"); // already in paise from frontend

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "hookspy_" + tier.toLowerCase() + "_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            log.info("Order request: {}", orderRequest);
            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Created order: {}", order);

            return ResponseEntity.ok(
                    Map.of("orderId", order.get("id"),
                            "keyId", razorpayConfig.getKeyId()));
        } catch (RazorpayException e) {
            return ResponseEntity.status(500)
                    .body(new APIError("PAYMENT_ERROR",
                            "Could not create payment order: " + e.getMessage()));
        }
    }

    // ── POST /api/payment/confirm ──────────────────────────────────────────────
    // Verifies Razorpay signature, then upgrades the user's tier
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, String> body) throws RazorpayException {
        String orderId = body.get("razorpayOrderId");
        String paymentId = body.get("razorpayPaymentId");
        String signature = body.get("razorpaySignature");
        String tier = body.get("tier");

        // ── HMAC-SHA256 signature verification ────────────────────────────────
        // This is the security fix from the handoff doc — don't skip this
        if (!razorpayService.verifySignature(orderId, paymentId, signature)) {
            return ResponseEntity.status(400)
                    .body(new APIError("INVALID_SIGNATURE", "Payment verification failed"));
        }

        // Upgrade the user's tier
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        try {
            userService.upgradeTier(email, tier);
            return ResponseEntity.ok(Map.of("status", "upgraded", "tier", tier));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new APIError("UPGRADE_ERROR",
                            "Payment verified but upgrade failed: " + e.getMessage()));
        }
    }


}