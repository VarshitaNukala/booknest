package com.booknest.controller;

import com.booknest.dto.request.CreatePaymentRequest;
import com.booknest.dto.response.PaymentResponse;
import com.booknest.entity.User;
import com.booknest.service.StripePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(stripePaymentService.createPaymentIntent(request));
    }

    @GetMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String paymentIntentId) {

        return ResponseEntity.ok(stripePaymentService.confirmPayment(paymentIntentId));
    }
}