package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class PaymentPersistenceServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentPersistenceService paymentPersistenceService;

    @Test
    @DisplayName("Should Save Payment And Order Successfully")
    void shouldSavePaymentAndOrderSuccessfully() {
        // given
        Payment payment = Payment.builder()
                .build();

        Order order = Order.builder()
                .build();

        // when
        paymentPersistenceService.savePaymentAndOrder(payment, order);

        // then
        then(paymentRepository).should().save(payment);
        then(orderRepository).should().save(order);
    }
}
