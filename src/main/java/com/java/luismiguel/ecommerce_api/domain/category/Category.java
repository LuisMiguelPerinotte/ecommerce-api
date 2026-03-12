package com.java.luismiguel.ecommerce_api.domain.category;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID categoryId;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    String name;

    @Column(name = "description", nullable = false, length = 500)
    String description;

    @Column(name = "slug", nullable = false)
    String slug;

    @Column(name = "active", nullable = false)
    Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    LocalDateTime createdAt;
}
