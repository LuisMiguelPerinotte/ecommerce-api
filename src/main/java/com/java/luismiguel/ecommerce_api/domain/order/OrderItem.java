package com.java.luismiguel.ecommerce_api.domain.order;

import com.java.luismiguel.ecommerce_api.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    String productName;

    @Column(name = "product_sku") // Opcional
    String productSku;

    @Column(name = "unit_price", nullable = false)
    BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "subtotal", nullable = false)
    BigDecimal subtotal;
}
