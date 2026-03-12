package com.java.luismiguel.ecommerce_api.domain.product;

import com.java.luismiguel.ecommerce_api.domain.category.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID productId;

    @Column(name = "name", nullable = false, length = 200)
    String name;

    @Column(name = "description", nullable = false, length = 2000)
    String description;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity;

    @Column(name = "image_url")
    String imageUrl; // Nesse momento é opcional. Mais para frente adicionar S3 para armazenar imagens

    @Column(name = "sku")
    String sku; // Nesse momento é opcional

    @Column(name = "active", nullable = false)
    Boolean active;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
