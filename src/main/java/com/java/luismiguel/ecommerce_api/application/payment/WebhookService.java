package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.InvalidWebhookSignatureException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class WebhookService {
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    public WebhookService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void processWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            switch (event.getType()) {
                case "checkout.session.completed":
                    paymentService.processCheckoutCompleted(event);
                    break;

                case "payment_intent.payment_failed":
                    paymentService.processPaymentFailed(event);
                    break;

                case "checkout.session.expired":
                    paymentService.processPaymentExpired(event);
                    break;

                default:
                    log.info("Ignored Event: type: {} id: {}", event.getType(), event.getId());
            }
        } catch (SignatureVerificationException e) {
            throw new InvalidWebhookSignatureException();
        }
    }
}
