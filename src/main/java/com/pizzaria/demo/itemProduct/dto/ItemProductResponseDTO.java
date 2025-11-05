package com.pizzaria.demo.itemProduct.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemProductResponseDTO(
        Integer id,
        Integer productId,
        String productName,
        Integer purchaseId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        LocalDateTime createdAt
) {
}