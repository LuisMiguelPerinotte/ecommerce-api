package com.java.luismiguel.ecommerce_api.api.dto.address.request;

import jakarta.validation.constraints.Size;

public record UpdateAddressRequestDTO(
        @Size(min = 3, max = 100)
        String street,

        @Size(min = 3, max = 100)
        String neighborhood,

        @Size(min = 1, max = 10)
        String number,

        @Size(max = 100)
        String complement,

        @Size(min = 8, max = 10)
        String zipCode
) {
}
