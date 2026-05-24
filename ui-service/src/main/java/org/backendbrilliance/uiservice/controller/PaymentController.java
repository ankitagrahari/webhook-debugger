package org.backendbrilliance.uiservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.backendbrilliance.uiservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/payment", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    @Value("${cashfree.app.id}")
    private String appId;

    @Value("${cashfree.secret.key}")
    private String secretKey;

    @Value("${cashfree.env:TEST}")
    private String env;

    @Value("${capture.baseurl:http://localhost:5173}")
    private String baseUrl;

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentController(UserService userService) {
        this.userService = userService;
    }

    private String cashFreeBaseUrl() {
        return "PROD".equalsIgnoreCase(env)
                ? "https://api.cashfree.com/pg"
                : "https://sandbox.cashfree.com/pg";
    }

    // ── POST /api/payment/order ──────────────────────────────────────────
    // Creates a Cashfree order, returns order_id + payment_session_id
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestBody Map<String, Object> body) throws Exception {

        String tier  = ((String) body.get("tier")).toUpperCase();
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        int amount = tier.equals("PRO") ? 299 : 799;
        String orderId = "HS-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12).toUpperCase();

        // Cashfree order payload
        String payload = mapper.writeValueAsString(Map.of(
                "order_id",       orderId,
                "order_amount",   amount,
                "order_currency", "INR",
                "customer_details", Map.of(
                        "customer_id",    email.replace("@", "_").replace(".", "_"),
                        "customer_email", email,
                        "customer_phone", "9999999999"
                ),
                "order_meta", Map.of(
                        "return_url", baseUrl + "/upgrade?order_id={order_id}&tier=" + tier,
                        "notify_url", baseUrl + "/api/payment/cashfree/webhook"
                ),
                "order_note", "HookSpy " + tier + " Plan"
        ));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cashFreeBaseUrl() + "/orders"))
                .header("Content-Type", "application/json")
                .header("x-api-version", "2023-08-01")
                .header("x-client-id", appId)
                .header("x-client-secret", secretKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());

        if (response.statusCode() != 200) {
            return ResponseEntity.status(500).body(Map.of(
                    "error",   "ORDER_FAILED",
                    "message", root.path("message").asText("Order creation failed")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "orderId",          orderId,
                "paymentSessionId", root.path("payment_session_id").asText(),
                "cfOrderId",        root.path("cf_order_id").asText()
        ));
    }

    // ── POST /api/payment/verify ─────────────────────────────────────────
    // Called after redirect back — verifies order status
    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestBody Map<String, String> body) throws Exception {

        String orderId = body.get("orderId");
        String tier    = body.get("tier");
        String email   = SecurityContextHolder.getContext().getAuthentication().getName();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cashFreeBaseUrl() + "/orders/" + orderId))
                .header("x-api-version", "2023-08-01")
                .header("x-client-id", appId)
                .header("x-client-secret", secretKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root   = mapper.readTree(response.body());
        String status   = root.path("order_status").asText();

        if ("PAID".equals(status)) {
            userService.upgradeTier(email, tier);
            return ResponseEntity.ok(Map.of(
                    "status", "upgraded",
                    "tier",   tier
            ));
        }

        return ResponseEntity.status(402).body(Map.of(
                "error",   "PAYMENT_PENDING",
                "message", "Payment status: " + status
        ));
    }

    // ── POST /api/payment/cashfree/webhook ───────────────────────────────
    // Cashfree calls this after payment — backup to the redirect flow
    @PostMapping("/cashfree/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody String body,
            @RequestHeader(value = "x-webhook-signature", required = false) String sig,
            @RequestHeader(value = "x-webhook-timestamp", required = false) String ts)
            throws Exception {

        // Verify signature: HMAC-SHA256 of (timestamp + body)
        if (sig != null && ts != null && !verifyWebhook(ts, body, sig)) {
            return ResponseEntity.status(401).build();
        }

        JsonNode root = mapper.readTree(body);
        String event  = root.path("type").asText();

        if ("PAYMENT_SUCCESS_WEBHOOK".equals(event)) {
            String orderId = root.path("data").path("order")
                    .path("order_id").asText();
            String email   = root.path("data").path("customer_details")
                    .path("customer_email").asText();

            // Derive tier from order_id prefix or amount
            int amount = root.path("data").path("payment")
                    .path("payment_amount").asInt(0);
            String tier = amount <= 299 ? "PRO" : "TEAM";

            try {
                userService.upgradeTier(email, tier);
            } catch (Exception ignored) {
                // Log but return 200 — prevents Cashfree retrying
            }
        }

        return ResponseEntity.ok().build();
    }

    // ── Webhook signature verification ───────────────────────────────────
    private boolean verifyWebhook(String timestamp, String body, String sig) {
        try {
            String data = timestamp + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secretKey.getBytes(), "HmacSHA256"));
            String computed = HexFormat.of()
                    .formatHex(mac.doFinal(data.getBytes()));
            return computed.equals(sig);
        } catch (Exception e) {
            return false;
        }
    }
}
