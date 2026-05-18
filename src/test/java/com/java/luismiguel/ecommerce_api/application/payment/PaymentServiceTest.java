package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.api.dto.payment.response.CheckoutResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
import com.java.luismiguel.ecommerce_api.domain.user.User;
import com.java.luismiguel.ecommerce_api.infrastructure.client.stripe.StripeCheckoutClient;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.order.OrderNotFoundException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.ErrorCreatingCheckoutException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.OrderCannotBePaidException;
import com.java.luismiguel.ecommerce_api.infrastructure.exception.business.payment.PaymentIsAlreadyInProgressException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentPersistenceService paymentPersistenceService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeCheckoutClient stripeCheckoutClient;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("createCheckout")
    class CreateCheckout {
        UUID orderId;
        UUID userId;

        @BeforeEach
        void setUp() {
            orderId = UUID.randomUUID();
            userId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should Throw Exception When Order Not Found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // given
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.empty());

            // when + then
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
                paymentService.createCheckout(orderId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Order Belongs To Another User")
        void shouldThrowExceptionWhenOrderBelongsToAnotherUser() {
            // given
            User anotherUser = User.builder()
                    .userId(UUID.randomUUID())
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(anotherUser)
                    .build();

            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));

            // when + then
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> {
                paymentService.createCheckout(orderId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Order Not Found!");
        }


        @Test
        @DisplayName("Should Throw Exception When Payment Is Already In Progress")
        void shouldThrowExceptionWhenPaymentIsAlreadyInProgress() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PENDING)
                    .build();

            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED)).willReturn(true);

            // when + then
            PaymentIsAlreadyInProgressException exception = assertThrows(PaymentIsAlreadyInProgressException.class, () -> {
                paymentService.createCheckout(orderId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("A Payment Is Already In Progress!");
        }


        @Test
        @DisplayName("Should Throw Exception When Order Cannot Be Paid")
        void shouldThrowExceptionWhenOrderCannotBePaid() {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PAID)
                    .build();

            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED)).willReturn(false);

            // when + then
            OrderCannotBePaidException exception = assertThrows(OrderCannotBePaidException.class, () -> {
                paymentService.createCheckout(orderId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("The Order Cannot Be Paid!");
        }


        @Test
        @DisplayName("Should Create Checkout Successfully")
        void shouldCreateCheckoutSuccessfully() throws StripeException {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.valueOf(100))
                    .build();

            Session session = mock(Session.class);

            given(session.getId()).willReturn("cs_test_123");
            given(session.getUrl()).willReturn("https://checkout.stripe.com/test");

            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED)).willReturn(false);
            given(stripeCheckoutClient.createCheckoutSession(order)).willReturn(session);

            // when
            CheckoutResponseDTO result = paymentService.createCheckout(orderId, userId);

            // then
            assertThat(result.stripePaymentUrl()).isEqualTo("https://checkout.stripe.com/test");
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

            then(paymentPersistenceService).should()
                    .savePaymentAndOrder(paymentCaptor.capture(), orderCaptor.capture());

            Payment payment = paymentCaptor.getValue();

            assertThat(payment.getStripeSessionId()).isEqualTo("cs_test_123");
            assertThat(payment.getOrder()).isEqualTo(order);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
            assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(payment.getCurrency()).isEqualTo("brl");

            assertThat(orderCaptor.getValue()).isEqualTo(order);
        }


        @Test
        @DisplayName("Should Throw Exception When Stripe Fails")
        void shouldThrowExceptionWhenStripeFails() throws StripeException {
            // given
            User user = User.builder()
                    .userId(userId)
                    .build();

            Order order = Order.builder()
                    .orderId(orderId)
                    .user(user)
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(BigDecimal.valueOf(100))
                    .build();

            StripeException stripeException = mock(StripeException.class);

            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(order));
            given(paymentRepository.existsByOrderAndStatus(order, PaymentStatus.CREATED)).willReturn(false);
            given(stripeCheckoutClient.createCheckoutSession(order)).willThrow(stripeException);

            // when + then
            ErrorCreatingCheckoutException exception = assertThrows(ErrorCreatingCheckoutException.class, () -> {
                paymentService.createCheckout(orderId, userId);
            });

            assertThat(exception.getMessage()).isEqualTo("Error Creating Checkout");

            then(paymentPersistenceService).should(never())
                    .savePaymentAndOrder(any(Payment.class), any(Order.class));
        }
    }
}
