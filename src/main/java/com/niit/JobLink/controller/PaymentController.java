package com.niit.joblink.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    // Safely pull the PAYAPI key directly from the operating system environment
    private static final String PAYSTACK_SECRET_KEY = System.getenv("PAYAPI");

    @PostMapping("/verify-backend")
    public ResponseEntity<String> verifyTransaction(@RequestParam("reference") String reference) {
        // Guard check to make sure your environment variable is actively loading
        if (PAYSTACK_SECRET_KEY == null || PAYSTACK_SECRET_KEY.isBlank()) {
            System.err.println("CRITICAL CONFIG ERROR: The 'PAYAPI' environment variable is missing!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server configuration error: Key missing.");
        }

        String url = "https://api.paystack.co/transaction/verify/" + reference;
        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            // Inject the dynamic key into the authorization header
            headers.set("Authorization", "Bearer " + PAYSTACK_SECRET_KEY);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                String status = (String) data.get("status");

                if ("success".equals(status)) {
                    return ResponseEntity.ok("Payment Verified Successfully!");
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed.");
            
        } catch (Exception e) {
            System.err.println("Paystack verification exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing transaction.");
        }
    }
}