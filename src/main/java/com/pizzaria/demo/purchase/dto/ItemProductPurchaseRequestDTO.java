package com.pizzaria.demo.purchase.dto;

import com.pizzaria.demo.itemProduct.model.ItemProduct;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ItemProductPurchaseRequestDTO(
        @NotNull
        @Positive
        Integer productId,

        @NotNull
        @Positive
        Integer quantity) {

}
