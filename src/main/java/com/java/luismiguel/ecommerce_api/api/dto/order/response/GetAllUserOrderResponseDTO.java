package com.java.luismiguel.ecommerce_api.api.dto.order.response;

import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetAllUserOrderResponseDTO(
        UUID orderId,
        List<ListOrderItemResponseDTO> items,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
