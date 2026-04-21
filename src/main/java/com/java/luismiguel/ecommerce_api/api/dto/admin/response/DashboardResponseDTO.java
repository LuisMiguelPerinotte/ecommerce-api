package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record DashboardResponseDTO(
        BigDecimal totalRevenue,
        Long totalOrders,
        Map<OrderStatus, Long> ordersByStatus,
        Long totalProducts,
        Integer lowStockProducts,
        Long totalUsers,
        BigDecimal revenueToday,
        Long ordersToday,
        LocalDateTime generatedAt
) {
}
