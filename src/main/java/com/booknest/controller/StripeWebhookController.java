package com.booknest.controller;

import com.booknest.entity.LendingTransaction;
import com.booknest.enums.BookStatus;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.repository.LendingTransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final LendingTransactionRepository transactionRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        // 1. Verify signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // 2. Handle different event types
        log.info("Stripe event received: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            case "charge.succeeded" -> log.info("Charge succeeded for event: {}", event.getId());
            case "payment_intent.created" -> log.info("PaymentIntent created: {}", event.getId());
            default -> log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("OK");
    }

    private void handlePaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = null;

        try {
            // Newer Stripe SDK way
            paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .deserializeUnsafe();
        } catch (Exception e) {
            log.error("Deserialization failed: {}", e.getMessage());
            return;
        }

        if (paymentIntent == null) {
            log.error("PaymentIntent is null");
            return;
        }

        String transactionId = paymentIntent.getMetadata().get("transaction_id");

        if (transactionId == null) {
            log.warn("No transaction_id in metadata");
            return;
        }

        LendingTransaction transaction = transactionRepository.findById(transactionId).orElse(null);

        if (transaction != null && transaction.getTransactionStatus() == BookStatus.AWAITING_PAYMENT) {
            transaction.getBook().setStatus(BookStatus.BORROWED);
            transaction.setTransactionStatus(BookStatus.BORROWED);
            transactionRepository.save(transaction);
            log.info("📚 Book '{}' now BORROWED", transaction.getBook().getTitle());
        }
    }
    private void handlePaymentFailed(Event event) {
        Optional<PaymentIntent> paymentIntentOpt = extractPaymentIntent(event);

        if (paymentIntentOpt.isEmpty()) return;

        PaymentIntent paymentIntent = paymentIntentOpt.get();
        String transactionId = paymentIntent.getMetadata().get("transaction_id");

        if (transactionId == null) return;

        log.error("❌ Payment FAILED for transaction: {}", transactionId);

        LendingTransaction transaction = transactionRepository.findById(transactionId)
                .orElse(null);

        if (transaction != null && transaction.getTransactionStatus() == BookStatus.AWAITING_PAYMENT) {
            transaction.getBook().setStatus(BookStatus.AVAILABLE);
            transaction.setTransactionStatus(BookStatus.AVAILABLE);
            transactionRepository.save(transaction);
            log.info("📚 Book '{}' back to AVAILABLE", transaction.getBook().getTitle());
        }
    }

    private Optional<PaymentIntent> extractPaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            StripeObject stripeObject = deserializer.getObject().get();
            if (stripeObject instanceof PaymentIntent) {
                return Optional.of((PaymentIntent) stripeObject);
            }
        }

        return Optional.empty();
    }
}