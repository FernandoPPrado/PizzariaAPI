package com.pizzaria.demo.product.dto;

import com.pizzaria.demo.product.model.Category;

import java.math.BigDecimal;

public record ProductResponseDTO(
        Integer id,
        String productName,
        String description,
        BigDecimal price,
        boolean active,
        Category category
) {
}
