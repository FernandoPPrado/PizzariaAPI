package com.pizzaria.demo.springSecurity.purchaseSecurity;

import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import com.pizzaria.demo.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("purchaseSecurity")
public class PurchaseSecurity {
    private final PurchaseRepository purchaseRepository;

    public PurchaseSecurity(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public boolean isOwner(Integer purchaseId, Authentication authentication) {
        User user = (User) authentication.getPrincipal(); // ou seu UserDetailsImpl
        return purchaseRepository.existsByIdAndUser_IdAndEnabledTrue(purchaseId, user.getId());
    }


}
