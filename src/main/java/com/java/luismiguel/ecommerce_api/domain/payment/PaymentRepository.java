package com.java.luismiguel.ecommerce_api.domain.payment;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrder(Order order);

    Boolean existsByMpPaymentId(String mpPaymentId);
}
