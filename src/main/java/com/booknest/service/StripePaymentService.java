package com.booknest.service;

import com.booknest.dto.request.CreatePaymentRequest;
import com.booknest.dto.response.PaymentResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class StripePaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    public PaymentResponse createPaymentIntent(CreatePaymentRequest request) {
        try {
            // Convert amount to paise (smallest currency unit for INR)
            long amountInPaise = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency("inr")
                    .putMetadata("transaction_id", request.getTransactionId())
                    .putMetadata("payment_type", request.getPaymentType())
                    .setDescription(getDescription(request.getPaymentType()))
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Payment intent created: {} for transaction: {}",
                    paymentIntent.getId(), request.getTransactionId());

            return PaymentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .status(paymentIntent.getStatus())
                    .message("Payment intent created successfully")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            return PaymentResponse.builder()
                    .status("failed")
                    .message("Payment failed: " + e.getMessage())
                    .build();
        }
    }

    public PaymentResponse confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            return PaymentResponse.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .status(paymentIntent.getStatus())
                    .message("Payment " + paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to retrieve payment: {}", e.getMessage());
            return PaymentResponse.builder()
                    .status("failed")
                    .message("Failed to retrieve payment")
                    .build();
        }
    }

    public PaymentResponse refundPayment(String paymentIntentId, BigDecimal amount) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", paymentIntentId);

            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                // Refund partial amount (for late fee deductions)
                Long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
                params.put("amount", amountInPaise);
            }

            // Using Stripe's refund API
            Refund refund = Refund.create(params);

            log.info("Refund processed: {} for payment: {}", refund.getId(), paymentIntentId);

            return PaymentResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .status(refund.getStatus())
                    .message("Refund processed: " + refund.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Refund failed: {}", e.getMessage());
            return PaymentResponse.builder()
                    .status("failed")
                    .message("Refund failed: " + e.getMessage())
                    .build();
        }
    }

    private String getDescription(String paymentType) {
        return switch (paymentType) {
            case "DEPOSIT" -> "Book lending deposit - refundable";
            case "SUBSCRIPTION" -> "Book club membership subscription";
            case "LATE_FEE" -> "Late return fee for borrowed book";
            default -> "BookNest payment";
        };
    }
}