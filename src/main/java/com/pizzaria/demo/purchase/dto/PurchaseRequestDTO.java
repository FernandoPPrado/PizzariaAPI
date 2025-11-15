package com.pizzaria.demo.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PurchaseRequestDTO(

        @NotNull
        List<ItemProductPurchaseRequestDTO> itemProduct

) {
}
