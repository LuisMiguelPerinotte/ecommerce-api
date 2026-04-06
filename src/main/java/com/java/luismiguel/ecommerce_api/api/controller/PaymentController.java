package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.payment.response.CheckoutResponseDTO;
import com.java.luismiguel.ecommerce_api.application.payment.PaymentService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment gateway integration and payment-related endpoints")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @RateLimiter(name = "create-resource")
    @Operation(summary = "Create Checkout", description = "Create a checkout preference for the given order and return checkout data (Mercado Pago).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Checkout created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error: invalid checkout request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    public ResponseEntity<CheckoutResponseDTO> createCheckout(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(paymentService.createCheckout(orderId, user.getUserId()), HttpStatus.CREATED);
    }


    @PostMapping("/webhook") // Only for Mercado Pago notifications
    @Hidden
    @Operation(hidden = true)
    public ResponseEntity<Void> webhook(
            @RequestParam(required = false) String type,
            @RequestParam(value = "data.id", required = false) String paymentId,
            @RequestParam(value = "x-signature", required = false) String signature,
            @RequestParam(value = "x-request-id", required = false) String requestId
    ) {
        if (type == null || paymentId == null) return ResponseEntity.ok().build();
        paymentService.processWebhook(type, paymentId, signature, requestId);
        return ResponseEntity.ok().build();
    }
}
