package org.backendbrilliance.uiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.backendbrilliance.uiservice.entity.User;
import org.backendbrilliance.uiservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.backendbrilliance.common.enums.Tier;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RazorpayService {

    private final UserRepository userRepository;

    @Value("${razorpay.key.id:test_key}")
    private String keyId;

    @Value("${razorpay.key.secret:test_secret}")
    private String keySecret;

    public RazorpayService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns Razorpay order config for the frontend checkout.
     * In production: create an order via Razorpay API and return order_id.
     * For MVP: return config directly, verify payment on callback.
     */
    public RazorpayOrderConfig createOrder(Tier tier, User user) {
        int amountPaise = switch (tier) {
            case PRO  -> 29900;  // ₹299
            case TEAM -> 79900;  // ₹799
            default   -> 0;
        };

        return new RazorpayOrderConfig(
                keyId,
                amountPaise,
                "INR",
                "HookSpy " + tier.name() + " Plan",
                user.getEmail(),
                user.getId().toString()
        );
    }

    /**
     * Called after Razorpay confirms payment.
     * In production: verify signature with HMAC-SHA256.
     * For MVP: trust the callback and upgrade tier.
     */
    @Transactional
    public void confirmUpgrade(UUID userId, Tier newTier, String paymentId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTier(newTier);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Upgraded user [id={}, tier={}, paymentId={}]",
                    userId, newTier, paymentId);
        });
    }

    public record RazorpayOrderConfig(
            String keyId,
            int amountPaise,
            String currency,
            String description,
            String email,
            String userId
    ) {}
}