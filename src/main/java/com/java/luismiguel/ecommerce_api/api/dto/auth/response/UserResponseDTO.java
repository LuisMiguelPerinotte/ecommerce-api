package com.java.luismiguel.ecommerce_api.api.dto.auth.response;

import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String username,
        String email,
        UserRole userRole,
        LocalDateTime createdAt
) {
}
