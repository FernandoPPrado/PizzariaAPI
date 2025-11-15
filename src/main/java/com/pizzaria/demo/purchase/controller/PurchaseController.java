package com.pizzaria.demo.purchase.controller;

import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseResponseDTO;
import com.pizzaria.demo.purchase.service.PurchaseService;
import com.pizzaria.demo.user.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;


    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    //seLogadoE#IdEIgualDoUser
    @PostMapping(path = "/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PurchaseResponseDTO> createPurchase(@RequestBody @Valid PurchaseRequestDTO purchaseRequestDTO, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(purchaseService.createPurchase(purchaseRequestDTO, user));
    }

    @GetMapping(path = "/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PurchaseResponseDTO>> listPurchaseByUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(purchaseService.listPurchaseByUser(user.getId()));
    }

    @PreAuthorize("@purchaseSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<PurchaseResponseDTO> getPurchaseById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PreAuthorize("@purchaseSecurity.isOwner(#id, authentication) or hasRole('ADMIN')")
    @PutMapping(path = "/{id}/cancel")
    public ResponseEntity<Void> cancelPurchaseById(@PathVariable Integer id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }
}
