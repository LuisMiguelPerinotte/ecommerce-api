package com.java.luismiguel.ecommerce_api.infrastructure.security.jwt;

import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import lombok.Builder;

import java.util.UUID;

@Builder
public record JwtUserData(
        UUID userId,
        String email,
        UserRole userRole
) {
}
