package com.java.luismiguel.ecommerce_api.api.dto.admin.request;

import com.java.luismiguel.ecommerce_api.domain.user.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequestDTO(
        @NotNull
        UserRole role
) {
}
