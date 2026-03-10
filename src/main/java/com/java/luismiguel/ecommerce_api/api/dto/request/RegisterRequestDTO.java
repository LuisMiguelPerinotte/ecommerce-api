package com.java.luismiguel.ecommerce_api.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank (message = "The Username is required!")
        @Size (min = 6, max = 50, message  = "The Username cannot be shorter than 6 characters or longer than 50!")
        @Pattern(
                regexp = "^[a-zA-Z0-9_.-]+$",
                message = "Only letters, numbers, periods, hyphens, and underscores."
        )
        String username,

        @NotBlank (message = "The E-mail is required!")
        @Size(max = 150)
        String email,

        @NotBlank (message = "The Password is required!")
        @Size (min = 8, message = "The Password must be at least 8 characters long!")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[@$!%*?&])[A-Za-z\\\\d@$!%*?&]+$",
                message = "It must include uppercase, lowercase, numbers, and special characters."
        )
        String password
) {
}
