package com.pizzaria.demo.product.dto;

import com.pizzaria.demo.product.model.Category;
import com.pizzaria.demo.product.model.Product;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.NonNull;

import java.math.BigDecimal;

public record ProductRequestDTO(
        @NotBlank
        @Size(min = 2, max = 30)
        String productName,
        @NotBlank
        @Size(min = 2, max = 250)
        String description,
        @NotNull
        @Positive
        BigDecimal price,
        boolean active,
        @NotNull
        Category category
) {

}
