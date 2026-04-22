package com.java.luismiguel.ecommerce_api.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @Query("""
        SELECT p.productId as productId,
               p.name as name,
               p.stockQuantity as stockQuantity
        FROM Product p
        WHERE p.stockQuantity < :threshold
            AND p.active = true
""")
    List<LowStockProductProjection> findLowStockProducts(@Param("threshold") Integer threshold);


    @Query("""
        SELECT COUNT(p)
        FROM Product p
        WHERE p.stockQuantity < :threshold
            AND p.active = true
""")
    Integer countLowStockProducts(@Param("threshold") Integer threshold);
}
