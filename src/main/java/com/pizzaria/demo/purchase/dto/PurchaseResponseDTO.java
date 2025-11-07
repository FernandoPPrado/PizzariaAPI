package com.pizzaria.demo.purchase.dto;

import com.pizzaria.demo.purchase.model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponseDTO(
        Integer purchaseId,
        Integer userId,
        BigDecimal total,
        Status status,
        LocalDateTime created,
        List<ItemProductPurchaseResponseDTO> items
) {
}
