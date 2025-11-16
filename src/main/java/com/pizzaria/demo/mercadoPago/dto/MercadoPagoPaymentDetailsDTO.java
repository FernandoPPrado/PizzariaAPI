package com.pizzaria.demo.mercadoPago.dto;

import jakarta.validation.constraints.NotBlank;

public record MercadoPagoPaymentDetailsDTO(
        @NotBlank
        String status,
        @NotBlank
        String external_reference
) {
}