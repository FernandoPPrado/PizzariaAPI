package com.pizzaria.demo.purchase.controller;

import com.pizzaria.demo.mercadoPago.dto.MercadoPagoPaymentLinkDTO;
import com.pizzaria.demo.mercadoPago.service.MercadoPagoService;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseResponseDTO;
import com.pizzaria.demo.purchase.service.PurchaseService;
import com.pizzaria.demo.user.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final MercadoPagoService mercadoPagoService;

    public PurchaseController(PurchaseService purchaseService, MercadoPagoService mercadoPagoService) {
        this.purchaseService = purchaseService;
        this.mercadoPagoService = mercadoPagoService;
    }

    //seLogadoE#IdEIgualDoUser
    @PostMapping(path = "/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MercadoPagoPaymentLinkDTO> createPurchase(@RequestBody @Valid PurchaseRequestDTO purchaseRequestDTO, @AuthenticationPrincipal User user) {

        log.info("Recebida requisição para criar purchase para userId = {}", user.getId());
        PurchaseResponseDTO responseDTO = purchaseService.createPurchase(purchaseRequestDTO, user);
        log.info("Purchase criada com sucesso na requisição id = {} total = {}", responseDTO.purchaseId(), responseDTO.total());
        log.debug("Chamando serviço do Mercado Pago para criar pagamento da purchaseId = {}", responseDTO.purchaseId());
        MercadoPagoPaymentLinkDTO linkPaymentDTO = mercadoPagoService.createPaymentForPurchase(responseDTO.purchaseId());
        log.info("Link de pagamento do Mercado Pago criado com sucesso para purchaseId = {}", responseDTO.purchaseId());
        return ResponseEntity.ok(linkPaymentDTO);
    }

    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PurchaseResponseDTO>> listPurchaseByUser(@AuthenticationPrincipal User user) {
        log.info("Recebida requisição para listar purchases do usuário userId = {}", user.getId());
        List<PurchaseResponseDTO> purchaseListDTO = purchaseService.listPurchaseByUser(user.getId());
        log.info("Quantidade de purchases retornadas para userId = {}: {}", user.getId(), purchaseListDTO.size());
        return ResponseEntity.ok(purchaseListDTO);
    }

    @PreAuthorize("@purchaseSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<PurchaseResponseDTO> getPurchaseById(@PathVariable Integer id) {
        log.info("Recebida requisição para buscar purchase por id = {}", id);
        PurchaseResponseDTO purchaseResponseDTO = purchaseService.getPurchaseById(id);
        log.info("Purchase retornada na requisição id = {} total = {} status = {}", purchaseResponseDTO.purchaseId(), purchaseResponseDTO.total(), purchaseResponseDTO.status());
        return ResponseEntity.ok(purchaseResponseDTO);
    }

    @PreAuthorize("@purchaseSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    @PutMapping(path = "/{id}/cancel")
    public ResponseEntity<Void> cancelPurchaseById(@PathVariable Integer id) {
        log.info("Recebida requisição para cancelar (soft delete) purchase id = {}", id);
        purchaseService.deletePurchase(id);
        log.info("Purchase cancelada/desativada com sucesso na requisição id = {}", id);
        return ResponseEntity.noContent().build();
    }
}
