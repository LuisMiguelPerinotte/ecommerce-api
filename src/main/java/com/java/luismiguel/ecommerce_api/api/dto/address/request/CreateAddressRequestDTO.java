package com.java.luismiguel.ecommerce_api.api.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequestDTO(
        @NotBlank
        @Size(min = 3, max = 100)
        String street,

        @NotBlank
        @Size(min = 3, max = 100)
        String neighborhood,

        @NotBlank
        @Size(min = 1, max = 10)
        String number,

        @Size(max = 100)
        String complement,

        @NotBlank
        @Size(min = 8, max = 10)
        String zipCode
) {
}
