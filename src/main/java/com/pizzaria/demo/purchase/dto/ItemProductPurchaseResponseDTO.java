package com.pizzaria.demo.purchase.dto;

public record ItemProductPurchaseResponseDTO(
        Integer productId,
        String productName,
        Integer quantity
) {
}

