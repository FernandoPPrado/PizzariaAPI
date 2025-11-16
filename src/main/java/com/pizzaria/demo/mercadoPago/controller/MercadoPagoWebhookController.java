package com.pizzaria.demo.mercadoPago.controller;

import com.pizzaria.demo.mercadoPago.dto.MercadoPagoWebhookDTO;
import com.pizzaria.demo.mercadoPago.service.MercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mercado-pago")
public class MercadoPagoWebhookController {

    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoWebhookController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody MercadoPagoWebhookDTO webhookDTO) {
        System.out.println(">>> Recebi webhook do MP: " + webhookDTO.data().id());
        mercadoPagoService.processWebhook(webhookDTO);
        return ResponseEntity.ok().build();
    }
}