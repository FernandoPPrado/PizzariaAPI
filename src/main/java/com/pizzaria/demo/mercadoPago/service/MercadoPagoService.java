package com.pizzaria.demo.mercadoPago.service;

import com.pizzaria.demo.mercadoPago.dto.*;
import com.pizzaria.demo.purchase.model.Purchase;
import com.pizzaria.demo.purchase.model.Status;
import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoService {

    private final RestTemplate restTemplate;
    private final PurchaseRepository purchaseRepository;

    private final String accessToken;
    private final String notificationUrl;
    private final String successUrl;
    private final String failureUrl;
    private final String pendingUrl;

    public MercadoPagoService(RestTemplate restTemplate, PurchaseRepository purchaseRepository, @Value("${mercadopago.access-token}") String accessToken, @Value("${mercadopago.notification-url}") String notificationUrl, @Value("${mercadopago.success-url}") String successUrl, @Value("${mercadopago.failure-url}") String failureUrl, @Value("${mercadopago.pending-url}") String pendingUrl) {
        this.restTemplate = restTemplate;
        this.purchaseRepository = purchaseRepository;
        this.accessToken = accessToken;
        this.notificationUrl = notificationUrl;
        this.successUrl = successUrl;
        this.failureUrl = failureUrl;
        this.pendingUrl = pendingUrl;
    }

    public MercadoPagoPaymentLinkDTO createPaymentForPurchase(Integer purchaseId) {
        // 1) Busca a Purchase
        Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> new EntityNotFoundException("COMPRA NAO LOCALIZADA"));

        // 2) Converte itens da Purchase para itens do Mercado Pago
        List<MercadoPagoPreferenceItem> items = purchase.getItems().stream().map(item -> new MercadoPagoPreferenceItem(item.getProduct().getProductName(),   // title
                item.getQuantity(),                   // quantity
                item.getUnitPrice(),                      // unit_price (BigDecimal)
                "BRL"                                 // currency_id
        )).toList();

        // 3) back_urls (URLs para redirecionar depois do pagamento)
        Map<String, String> backUrls = Map.of("success", successUrl, "failure", failureUrl, "pending", pendingUrl);

        // 4) external_reference = id da Purchase
        String externalReference = purchase.getId().toString();

        // 5) Monta o corpo da requisição para o Mercado Pago
        MercadoPagoPreferenceRequest requestBody = new MercadoPagoPreferenceRequest(items, backUrls, notificationUrl, externalReference);

        // 6) Headers com Authorization e JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Authorization: Bearer <token>

        HttpEntity<MercadoPagoPreferenceRequest> entity = new HttpEntity<>(requestBody, headers);

        // 7) Faz o POST para a API do Mercado Pago
        ResponseEntity<MercadoPagoPreferenceResponse> response = restTemplate.postForEntity("https://api.mercadopago.com/checkout/preferences", entity, MercadoPagoPreferenceResponse.class);

        MercadoPagoPreferenceResponse pref = response.getBody();
        if (pref == null) {
            throw new IllegalStateException("Erro ao criar preferência no Mercado Pago");
        }

        // 8) Retorna DTO que seu controller vai expor para o front
        return new MercadoPagoPaymentLinkDTO(purchase.getId(), pref.init_point(), pref.sandbox_init_point(), externalReference);
    }


    public void processWebhook(MercadoPagoWebhookDTO webhookDTO) {

        // 1) Valida estrutura do webhook
        if (webhookDTO.data() == null || webhookDTO.data().id() == null) {
            return;
        }

        // paymentId REAL do Mercado Pago
        String paymentId = webhookDTO.data().id();

        // 2) Monta URL para consultar o pagamento
        String url = "https://api.mercadopago.com/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            // 3) Consulta pagamento no Mercado Pago
            ResponseEntity<MercadoPagoPaymentDetailsDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, MercadoPagoPaymentDetailsDTO.class);

            MercadoPagoPaymentDetailsDTO paymentDetails = response.getBody();

            if (paymentDetails == null || paymentDetails.external_reference() == null) {
                return;
            }

            // 4) O external_reference é o purchaseId
            Integer purchaseId = Integer.valueOf(paymentDetails.external_reference());

            // 5) Busca a purchase no banco
            Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> new EntityNotFoundException("COMPRA NAO LOCALIZADA NO WEBHOOK"));

            // 6) Idempotência simples (evita reprocessar o mesmo payment várias vezes)
            if (purchase.getPaymentId() != null && purchase.getPaymentId().equals(Long.valueOf(paymentId))) {
                // Já processado — IGNORA
                return;
            }

            // 7) Salva o paymentId do Mercado Pago
            purchase.setPaymentId(Long.valueOf(paymentId));

            // 8) Atualiza o status com base no Mercado Pago
            String mpStatus = paymentDetails.status();
            if (mpStatus == null) return;

            switch (mpStatus.toLowerCase()) {
                case "approved" -> purchase.setStatus(Status.COMPLETED);
                case "rejected" -> purchase.setStatus(Status.REJECTED);
                case "cancelled", "canceled" -> purchase.setStatus(Status.CANCELED);
                case "pending" -> purchase.setStatus(Status.PENDING);
                default -> {
                    return; // ignora status desconhecido
                }
            }

            // 9) Salva tudo no banco
            purchaseRepository.save(purchase);

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {

            // 10) Payment inexistente no MP (comum em eventos de teste)
            System.out.println("Pagamento " + paymentId + " não encontrado no Mercado Pago (404)");
        }
    }


}
