package com.java.luismiguel.ecommerce_api.api.dto.response;

import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreatedOrderResponseDTO(
        UUID orderId,
        OrderStatus orderStatus,
        List<ListOrderItemResponseDTO> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
