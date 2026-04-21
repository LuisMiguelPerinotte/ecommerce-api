package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetUsersResponseDTO(
        UUID userId,
        String username,
        String email,
        UserRole userRole,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
