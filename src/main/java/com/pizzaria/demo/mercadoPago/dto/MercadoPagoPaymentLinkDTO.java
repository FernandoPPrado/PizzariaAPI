package com.pizzaria.demo.mercadoPago.dto;

public record MercadoPagoPaymentLinkDTO(
        Integer purchaseId,
        String initPoint,
        String sandboxInitPoint,
        String externalReference
) {
}
