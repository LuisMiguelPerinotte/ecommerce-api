package com.java.luismiguel.ecommerce_api.api.dto.address.response;

import java.util.UUID;

public record GetAddressResponseDTO(
        UUID addressId,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        Boolean isDefault
) {
}
