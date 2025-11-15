package com.pizzaria.demo.purchase.service;

import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.itemProduct.repository.ItemProductRepository;
import com.pizzaria.demo.itemProduct.service.ItemProductService;
import com.pizzaria.demo.product.service.ProductService;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseResponseDTO;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseResponseDTO;
import com.pizzaria.demo.purchase.model.Purchase;
import com.pizzaria.demo.purchase.model.Status;
import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import com.pizzaria.demo.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {


    private final ItemProductService itemProductService;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(ItemProductService itemProductService, UserRepository userRepository, PurchaseRepository purchaRepository) {

        this.itemProductService = itemProductService;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaRepository;
    }


    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO purchaseRequestDTO, User owner) {

        Purchase purchase = new Purchase();
        purchase.setUser(userRepository.findByIdAndEnabledTrue(owner.getId()).orElseThrow(() -> new EntityNotFoundException("USUARIO NAO LOCALIZADO")));

        List<ItemProduct> itemProductList = itemProductService.createItemsForPurchase(purchaseRequestDTO.itemProduct(), purchase);
        purchase.setItems(itemProductList);
        purchase.calculateTotal();
        Purchase saved = purchaseRepository.save(purchase);
        return entityToResponse(saved);
    }

    public PurchaseResponseDTO getPurchaseById(Integer id) {
        Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> new EntityNotFoundException("PURCHASE NAO ENCONTRADA"));
        return entityToResponse(purchase);
    }

    public List<PurchaseResponseDTO> listPurchaseByUser(Integer userId) {
        return purchaseRepository.findAllByUser_IdAndEnabledTrue(userId).stream().map(this::entityToResponse).toList();
    }


    public Status updatePurchaseStatus(Integer purchaseId, Status status) {
        Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> new EntityNotFoundException("PURCHASE NAO ENCONTRADA"));
        purchase.setStatus(status);
        Purchase saved = purchaseRepository.save(purchase);
        return saved.getStatus();
    }

    public void deletePurchase(Integer purchaseId) {
        Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> new EntityNotFoundException("PURCHASE NAO ENCONTRADA"));
        purchase.setEnabled(false);
        purchaseRepository.save(purchase);
    }


    private PurchaseResponseDTO entityToResponse(Purchase purchase) {
        return new PurchaseResponseDTO(purchase.getId(), purchase.getUser().getId(), purchase.getTotal(), purchase.getStatus(), purchase.getCreated(), purchase.getItems().stream().map(e -> new ItemProductPurchaseResponseDTO(e.getProduct().getId(), e.getProduct().getProductName(), e.getQuantity())).toList());
    }

}
