package com.java.luismiguel.ecommerce_api.api.dto.response;

import java.util.UUID;

public record GetAddressResponseDTO(
        UUID addressId,
        String street,
        String complement,
        String state,
        String zipCode,
        String country
) {
}
