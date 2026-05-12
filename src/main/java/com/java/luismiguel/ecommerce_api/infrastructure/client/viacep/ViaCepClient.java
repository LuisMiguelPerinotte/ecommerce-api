package com.java.luismiguel.ecommerce_api.infrastructure.client.viacep;

import com.java.luismiguel.ecommerce_api.api.dto.address.request.AddressDataFromRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ViaCepClient {
    private final RestTemplate restTemplate;

    public ViaCepClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AddressDataFromRequestDTO getAddressData(String zipCode) {
        return restTemplate.getForObject(
                String.format("https://viacep.com.br/ws/%s/json", zipCode),
                AddressDataFromRequestDTO.class
        );
    }
}
