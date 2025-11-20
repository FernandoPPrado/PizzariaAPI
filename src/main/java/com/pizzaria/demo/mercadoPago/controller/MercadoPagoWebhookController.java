package com.pizzaria.demo.mercadoPago.controller;

import com.pizzaria.demo.mercadoPago.dto.MercadoPagoWebhookDTO;
import com.pizzaria.demo.mercadoPago.service.MercadoPagoService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mercado-pago")
public class MercadoPagoWebhookController {

    private final MercadoPagoService mercadoPagoService;

    private final String webhookSecret;

    public MercadoPagoWebhookController(MercadoPagoService mercadoPagoService, @Value("${webhook.secret}") String webhookSecret) {
        this.mercadoPagoService = mercadoPagoService;
        this.webhookSecret = webhookSecret;

    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam(name = "secret", required = false) String secret, @RequestBody MercadoPagoWebhookDTO webhookDTO) {
        log.info("Webhook do MercadoPago, iniciando validacao");

        if (secret == null || secret.isBlank() || !secret.equals(webhookSecret)) {
            log.warn("Webhook do Mercado Pago com secret inválido ou ausente");
            return ResponseEntity.status(403).build();
        }
        log.info("Webhook do Mercado Pago recebido com secret válido");
        mercadoPagoService.processWebhook(webhookDTO);
        return ResponseEntity.ok().build();
    }
}