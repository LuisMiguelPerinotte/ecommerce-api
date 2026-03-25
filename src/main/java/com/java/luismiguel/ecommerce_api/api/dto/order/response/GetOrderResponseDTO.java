package com.java.luismiguel.ecommerce_api.api.dto.order.response;

import com.java.luismiguel.ecommerce_api.api.dto.address.response.GetAddressResponseDTO;
import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetOrderResponseDTO(
        UUID orderId,
        List<ListOrderItemResponseDTO> items,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        GetAddressResponseDTO shippingAddress,
        String userNotes,
        LocalDateTime createdAt
) {
}
