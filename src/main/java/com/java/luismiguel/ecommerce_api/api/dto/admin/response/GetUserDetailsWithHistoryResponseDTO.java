package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetUserDetailsWithHistoryResponseDTO(
        UUID userId,
        String username,
        String email,
        UserRole userRole,
        Page<OrdersHistoryResponseDTO> orders,
        LocalDateTime createdAt
) {
}
