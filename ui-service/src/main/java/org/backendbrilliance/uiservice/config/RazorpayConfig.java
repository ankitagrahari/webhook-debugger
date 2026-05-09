package org.backendbrilliance.uiservice.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    String keyId;

    @Value("${razorpay.key.secret}")
    String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        log.info("Creating RazorpayClient with keyId=[{}] secretLength=[{}]",
                keyId, keySecret != null ? keySecret.length() : "NULL");

        // Trim defensively — YAML sometimes includes invisible whitespace
        String cleanKeyId = keyId.trim();
        String cleanSecret = keySecret.trim();

        log.info("Trimmed keyId=[{}]", cleanKeyId);

        return new RazorpayClient(keyId, keySecret);
    }
}
