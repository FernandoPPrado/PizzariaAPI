package com.pizzaria.demo.itemProduct.dto;

import java.math.BigDecimal;

public record ItemProductRequestDTO(
        Integer productId,
        Integer purchaseId,
        Integer quantity,
        BigDecimal unitPrice
) {
}

