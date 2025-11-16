package com.pizzaria.demo.mercadoPago.dto;

import java.math.BigDecimal;

public record MercadoPagoPreferenceItem(
        String title,
        Integer quantity,
        BigDecimal unit_price,
        String currency_id
) {
}