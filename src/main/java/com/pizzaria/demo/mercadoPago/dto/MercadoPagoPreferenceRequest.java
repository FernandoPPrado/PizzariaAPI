package com.pizzaria.demo.mercadoPago.dto;

import java.util.List;
import java.util.Map;

public record MercadoPagoPreferenceRequest(


        List<MercadoPagoPreferenceItem> items,

        Map<String, String> back_urls,

        String notification_url,

        // Referência externa — aqui será SEMPRE o purchaseId
        String external_reference

) {
}