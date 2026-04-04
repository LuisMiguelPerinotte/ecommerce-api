package com.java.luismiguel.ecommerce_api.api.dto.address.request;

public record AddressDataFromRequestDTO(
        String cep,
        String logradouro,
        String complemento,
        String unidade,
        String bairro,
        String localidade,
        String uf,
        String estado,
        String regiao,
        String ibge,
        String gia,
        String ddd,
        String siafi,
        Boolean erro
) {
}
