package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.api.dto.response.CheckoutResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.*;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${spring.mercadopago.webhook-secret}")
    private String webhookSecret;

    @Value("${spring.mercadopago.notification-url}")
    private String mpNotificationUrl;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public CheckoutResponseDTO createCheckout(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        if (!order.getUser().getUserId().equals(userId)) {
            throw new OrderNotFoundException();
        }

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Pedido #" + orderId)
                .quantity(1)
                .unitPrice(order.getTotalAmount())
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(orderId.toString())
                .notificationUrl(mpNotificationUrl)
                .build();

        PreferenceClient client = new PreferenceClient();

        try {
            Preference preference = client.create(preferenceRequest);
            Payment payment = Payment.builder()
                    .order(order)
                    .mpPreferenceId(preference.getId())
                    .externalReference(orderId.toString())
                    .status(PaymentStatus.CREATED)
                    .amount(order.getTotalAmount())
                    .build();

            order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
            orderRepository.save(order);
            paymentRepository.save(payment);
            return new CheckoutResponseDTO(preference.getInitPoint());

        } catch (MPApiException | MPException e) {
            throw new ErrorCreatingPreferenceException();
        }
    }


    @Async
    @Transactional
    public void processWebhook(String type, String paymentId, String signature, String requestId) {
        log.info("Webhook Received - type: {}, paymentId: {}", type, paymentId);

        if (!type.equals("payment")) {
            return;
        }

        if (paymentId.equals("123456")) {
            log.info("Test Webhook {} Received, Ignoring...", paymentId);
            return;
        }

        verifySignature(paymentId, requestId, signature);

        if (paymentRepository.existsByMpPaymentId(paymentId)) {
            log.info("Payment {} is Already Processed, Ignoring Webhook...", paymentId);
            return;
        }


        try{
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(paymentId));

            UUID orderId = UUID.fromString(mpPayment.getExternalReference());

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(OrderNotFoundException::new);

            if (mpPayment.getTransactionAmount().compareTo(order.getTotalAmount()) != 0) {
                log.warn("Amount mismatch for order {}!", orderId);
                return;
            }

            if (order.getOrderStatus() != OrderStatus.AWAITING_PAYMENT) {
                log.info("Order {} Not in AWAITING_PAYMENT Status, Ignoring...", orderId);
                return;
            }

            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(PaymentNotFoundException::new);

            if (payment.getStatus() == PaymentStatus.APPROVED ||
                    payment.getStatus() == PaymentStatus.REJECTED ||
                    payment.getStatus() == PaymentStatus.REFUNDED
            ) {
                log.info("Payment {} is Already Processed, Ignoring Webhook...", paymentId);
                return;
            }

            log.info("Payment Status: {}", mpPayment.getStatus());

            switch (mpPayment.getStatus()) {
                case "approved" -> {
                    order.setOrderStatus(OrderStatus.PAID);
                    payment.setStatus(PaymentStatus.APPROVED);
                    payment.setMpPaymentId(paymentId);
                }
                case "rejected", "cancelled" -> {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    payment.setStatus(PaymentStatus.REJECTED);
                }
                case "refunded" -> {
                    order.setOrderStatus(OrderStatus.REFUNDED);
                    payment.setStatus(PaymentStatus.REFUNDED);
                }
                case "in_process", "pending" -> {
                   payment.setStatus(PaymentStatus.PENDING);
                   log.info("Pending Payment to Order {}",  order.getOrderId());
                }
            }

            orderRepository.save(order);
            paymentRepository.save(payment);

            log.info("Updated {} Order for {}", order.getOrderId(), order.getOrderStatus());

        } catch (MPApiException | MPException e) {
            log.error("Error processing webhook for paymentId: {}", paymentId, e);;
        }
    }


    //Private Methods
    private void verifySignature(String dataId, String requestId, String xSignature) {
        try {
            String[] parts = xSignature.split(",");
            String ts = parts[0].split("=")[1];
            String v1 = parts[1].split("=")[1];

            String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256"));
            String generated = HexFormat.of().formatHex(mac.doFinal(manifest.getBytes()));

            if (!generated.equals(v1)) {
                throw new InvalidWebhookSignatureException();
            }

        } catch (Exception e) {
            throw new InvalidWebhookSignatureException();
        }
    }
}
