package com.java.luismiguel.ecommerce_api.domain.payment;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM Payment p
        JOIN FETCH p.order
        WHERE p.stripeSessionId = :id
""")
    Optional<Payment> findByStripeSessionIdWithOrder(@Param("id") String stripeSessionId);

    @Query("""
    SELECT p FROM Payment p
    JOIN FETCH p.order o
    WHERE o.orderId = :orderId
    AND p.status = :status
""")
    Optional<Payment> findPaymentByOrderIdAndStatusWithOrder(
            UUID orderId,
            PaymentStatus status
    );

    Boolean existsByOrderAndStatus(Order order, PaymentStatus status);
}
