package com.java.luismiguel.ecommerce_api.domain.payment;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(name = "stripe_session_id", unique = true)
    String stripeSessionId;

    @Column(name = "payment_intent_id", unique = true)
    String paymentIntentId;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "currency")
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    PaymentStatus status;

    @Column(name = "failure_reason")
    String failureReason;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @Column(name = "failed_at")
    LocalDateTime failedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;
}
