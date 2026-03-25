package com.java.luismiguel.ecommerce_api.api.controller;

import com.java.luismiguel.ecommerce_api.api.dto.payment.response.CheckoutResponseDTO;
import com.java.luismiguel.ecommerce_api.application.payment.PaymentService;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Pagamento", description = "")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Fazer Checkout", description = "")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User user
    ) {
        return new ResponseEntity<>(paymentService.createCheckout(orderId, user.getUserId()), HttpStatus.CREATED);
    }


    @PostMapping("/webhook") // Somente para chegada das NOTIFICAÇÕES enviadas pelo Mercado Pago.
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
