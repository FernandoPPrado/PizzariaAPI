package com.pizzaria.demo.mercadoPago.dto;

public record MercadoPagoWebhookDTO(
        Long id,
        String type,
        String action,
        Data data
) {
    public record Data(
            String id
    ) {
    }
}
