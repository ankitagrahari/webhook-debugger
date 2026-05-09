package org.backendbrilliance.uiservice.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.dtos.APIError;
import org.backendbrilliance.uiservice.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/api/payment", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final UserService userService;
    private final RazorpayClient razorpayClient;
    private final String keyId;
    private final String keySecret;

    public PaymentController(UserService userService,
                             @Value("${razorpay.key.id}") String keyId,
                             @Value("${razorpay.key.secret}") String keySecret) throws RazorpayException {
        this.userService = userService;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        log.info("Razorpay keyId loaded: {}", keyId != null ? keyId.substring(0, 8) + "..." : "NULL");
    }

    // ── POST /api/payment/order ────────────────────────────────────────────────
    // Creates a Razorpay order and returns orderId + keyId to the frontend
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            String tier = (String) body.get("tier");
            int amount = (Integer) body.get("amount"); // already in paise from frontend

            JSONObject options = new JSONObject();
            options.put("amount", amount);
            options.put("currency", "INR");
            options.put("receipt", "hookspy_" + tier.toLowerCase() + "_" + System.currentTimeMillis());
            options.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(options);

            return ResponseEntity.ok(Map.of(
                    "orderId", order.get("id"),
                    "keyId", keyId
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.status(500)
                    .body(new APIError("PAYMENT_ERROR", "Could not create payment order: " + e.getMessage()));
        }
    }

    // ── POST /api/payment/confirm ──────────────────────────────────────────────
    // Verifies Razorpay signature, then upgrades the user's tier
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody Map<String, String> body) {
        String orderId = body.get("razorpayOrderId");
        String paymentId = body.get("razorpayPaymentId");
        String signature = body.get("razorpaySignature");
        String tier = body.get("tier");

        // ── HMAC-SHA256 signature verification ────────────────────────────────
        // This is the security fix from the handoff doc — don't skip this
        if (!verifySignature(orderId, paymentId, signature)) {
            return ResponseEntity.status(400)
                    .body(new APIError("INVALID_SIGNATURE", "Payment verification failed"));
        }

        // Upgrade the user's tier
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.upgradeTier(email, tier);
            return ResponseEntity.ok(Map.of("status", "upgraded", "tier", tier));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new APIError("UPGRADE_ERROR", "Payment verified but upgrade failed: " + e.getMessage()));
        }
    }

    // ── HMAC-SHA256 verification ───────────────────────────────────────────────
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
            String generated = HexFormat.of().formatHex(mac.doFinal(payload.getBytes()));
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}