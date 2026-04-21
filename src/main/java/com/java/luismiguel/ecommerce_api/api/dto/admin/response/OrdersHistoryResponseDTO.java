package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdersHistoryResponseDTO(
        UUID orderId,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
