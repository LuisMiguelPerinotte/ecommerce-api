package com.java.luismiguel.ecommerce_api.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.items
        WHERE o.orderId = :id
""")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items
            WHERE o.user.userId = :userId
""")
    Page<Order> findByUserUserId(@Param("userId") UUID userId, Pageable pageable);


    @Query("""
            SELECT o FROM Order o
            JOIN o.user u
            WHERE u.userId = :userId
""")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);


    @Query("""
            SELECT SUM (o.totalAmount)
            FROM Order o
            WHERE o.orderStatus IN ('PAID', 'SHIPPED', 'DELIVERED')
                AND o.createdAt BETWEEN :start AND :end
""")
    BigDecimal getTotalRevenueByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')
            AND o.createdAt BETWEEN :start AND :end
""")
    Long countOrdersByPeriod(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);


    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.orderStatus IN ('PAID', 'SHIPPED', 'DELIVERED')
""")
    BigDecimal getTotalRevenue();


    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')
    """)
    Long countTotalOrders();


    @Query("""
        SELECT o.orderStatus as status, COUNT(o) as count
        FROM Order o
        GROUP BY o.orderStatus
    """)
    List<OrderStatusCountProjection> getOrderCountByStatus();


    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.orderStatus = 'PAID'
          AND o.createdAt >= :startOfDay
          AND o.createdAt < :endOfDay
    """)
    BigDecimal getRevenueToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);


    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')
          AND o.createdAt >= :startOfDay
          AND o.createdAt < :endOfDay
    """)
    Long countOrdersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
