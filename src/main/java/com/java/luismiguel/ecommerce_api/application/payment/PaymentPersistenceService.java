package com.java.luismiguel.ecommerce_api.application.payment;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderRepository;
import com.java.luismiguel.ecommerce_api.domain.payment.Payment;
import com.java.luismiguel.ecommerce_api.domain.payment.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PaymentPersistenceService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentPersistenceService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void savePaymentAndOrder(Payment payment, Order order) {
        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}
