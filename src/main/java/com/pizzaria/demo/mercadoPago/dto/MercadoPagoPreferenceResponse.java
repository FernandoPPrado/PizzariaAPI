package com.pizzaria.demo.mercadoPago.dto;


public record MercadoPagoPreferenceResponse(
        String id,
        String init_point,
        String sandbox_init_point
) {
}