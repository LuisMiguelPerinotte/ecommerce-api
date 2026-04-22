package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.api.dto.payment.response.CheckoutResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.*;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentPersistenceService paymentPersistenceService;
    private final PaymentRepository paymentRepository;

    private static final Set<OrderStatus> PAYABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.PAYMENT_FAILED);
    private static final Set<PaymentStatus> EXPIRATION_STATUSES = Set.of(PaymentStatus.CREATED, PaymentStatus.FAILED);

    public PaymentService(OrderRepository orderRepository, PaymentPersistenceService paymentPersistenceService, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentPersistenceService = paymentPersistenceService;
        this.paymentRepository = paymentRepository;
    }

    public CheckoutResponseDTO createCheckout(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(OrderNotFoundException::new);

        if (!userId.equals(order.getUser().getUserId())) {
            throw new OrderNotFoundException();
        }

        Boolean exists = paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED);
        if (exists) {
            throw new PaymentIsAlreadyInProgressException();
        }

        if (!PAYABLE_STATUSES.contains(order.getOrderStatus())) {
            throw new OrderCannotBePaidException();
        }


        List<SessionCreateParams.LineItem> items = createLineItemList(order);

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setClientReferenceId(orderId.toString())
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .putMetadata("orderId", orderId.toString())
                                        .build()
                        )
                        .addAllLineItem(items)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addExpand("payment_intent")
                        .build();

        try {
            Session session = Session.create(params);
            Payment payment = Payment.builder()
                    .stripeSessionId(session.getId())
                    .order(order)
                    .status(PaymentStatus.CREATED)
                    .amount(order.getTotalAmount())
                    .currency("brl")
                    .build();

            order.setOrderStatus(OrderStatus.AWAITING_PAYMENT);
            paymentPersistenceService.savePaymentAndOrder(payment, order);
            log.info("Created Checkout: orderId -> {} sessionId -> {}", orderId, session.getId());

            return new CheckoutResponseDTO(
                    session.getUrl()
            );

        } catch (StripeException e) {
            log.info("Error Creating Checkout for Order ID: {}", orderId);
            throw new ErrorCreatingCheckoutException();
        }
    }


    private List<SessionCreateParams.LineItem> createLineItemList(Order order) {
        List<SessionCreateParams.LineItem> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            Long productQuantity = item.getQuantity().longValue();
            String productName = item.getProductName();
            Long unitAmount = item.getUnitPrice()
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();


            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(productName)
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(unitAmount)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams
                            .LineItem.builder()
                            .setQuantity(productQuantity)
                            .setPriceData(priceData)
                            .build();

            items.add(lineItem);
        }
        return items;
    }


    @Transactional
    public void processCheckoutCompleted(Event event) {
        StripeObject stripeObject = deserializeObject(event);

        if (stripeObject instanceof Session session) {
            if ("paid".equals(session.getPaymentStatus())) {
                Optional<Payment> optionalPayment = paymentRepository.findByStripeSessionIdWithOrder(session.getId());
                if (optionalPayment.isEmpty()) {
                    log.warn("Payment not found...");
                    return;
                }

                Payment payment = optionalPayment.get();

                if (PaymentStatus.APPROVED.equals(payment.getStatus())) {
                    log.info("Payment (Payment ID: {}) is Already Processed, Ignoring...", payment.getPaymentId());
                    return;
                }

                Order order = payment.getOrder();

                payment.setStatus(PaymentStatus.APPROVED);
                payment.setPaidAt(LocalDateTime.now());
                order.setOrderStatus(OrderStatus.PAID);

                paymentRepository.save(payment);
                orderRepository.save(order);
                log.info("Approved Payment: paymentId -> {} orderId -> {}", payment.getPaymentId(), order.getOrderId());
            }
        }
    }

    @Transactional
    public void processPaymentFailed(Event event) {
        StripeObject stripeObject = deserializeObject(event);

        if (stripeObject instanceof PaymentIntent paymentIntent) {
            String orderId = paymentIntent.getMetadata().get("orderId");
            if (orderId.isEmpty()) {
                log.warn("orderId was not included in the metadata.");
                return;
            }

            Optional<Payment> optionalPayment = paymentRepository.findPaymentByOrderIdAndStatusWithOrder(UUID.fromString(orderId), PaymentStatus.CREATED);
            if (optionalPayment.isEmpty()) {
                log.warn("payment with orderId -> {} not found...", orderId);
                return;
            }

            Payment payment = optionalPayment.get();
            Order order = payment.getOrder();

            if (payment.getStatus() != PaymentStatus.CREATED) {
                log.info("payment (id -> {}) without the status CREATED. Ignoring...", payment.getPaymentId());
                return;
            }

            String failureReason = paymentIntent.getLastPaymentError().getDeclineCode();
            if (failureReason == null) failureReason = "unknown";

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailedAt(LocalDateTime.now());
            payment.setFailureReason(failureReason);

            paymentRepository.save(payment);
            orderRepository.save(order);
            log.info("Payment Failed: paymentId -> {} orderId -> {} failureReason -> {}",
                    payment.getPaymentId(), order.getOrderId(), failureReason);
        }
    }

    @Transactional
    public void processPaymentExpired(Event event) {
        StripeObject stripeObject = deserializeObject(event);

        if (stripeObject instanceof Session session) {
            Optional<Payment> optionalPayment = paymentRepository.findByStripeSessionIdWithOrder(session.getId());
            if (optionalPayment.isEmpty()) {
                log.warn("payment not found...");
                return;
            }

            Payment payment = optionalPayment.get();
            Order order = payment.getOrder();


            if (!EXPIRATION_STATUSES.contains(payment.getStatus())) {
                log.info("expired payment (id -> {}) without the status CREATED or FAILED. Ignoring...", payment.getPaymentId());
                return;
            }

            payment.setStatus(PaymentStatus.EXPIRED);
            payment.setFailedAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);

            paymentRepository.save(payment);
            orderRepository.save(order);
            log.info("expired payment: paymentId -> {} orderId -> {} sessionId -> {}", payment.getPaymentId(), order.getOrderId(), session.getId());
        }
    }


    private StripeObject deserializeObject(Event event) {
        StripeObject stripeObject = null;
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();

        if (optionalStripeObject.isPresent()) {
            stripeObject = optionalStripeObject.get();
        } else {
            try {
                stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();

            } catch (EventDataObjectDeserializationException e) {
                throw new ErrorOnTheObjectDeserializationException();
            }
        }
        return stripeObject;
    }
}
