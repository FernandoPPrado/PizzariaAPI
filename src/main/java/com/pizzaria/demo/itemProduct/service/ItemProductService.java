package com.pizzaria.demo.itemProduct.service;

import com.pizzaria.demo.itemProduct.dto.ItemProductRequestDTO;
import com.pizzaria.demo.itemProduct.dto.ItemProductResponseDTO;
import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.itemProduct.repository.ItemProductRepository;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.model.Purchase;
import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemProductService {

    @Autowired
    private ItemProductRepository itemProductRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;


    public ItemProductResponseDTO createItemProduct(ItemProductRequestDTO itemProductDTO) {

        Product product = productRepository.findByIdAndEnabledTrue(itemProductDTO.productId()).orElseThrow(() -> new EntityNotFoundException("PRODUTO NAO ENCONTRADO"));
        Purchase purchase = purchaseRepository.findById(itemProductDTO.purchaseId()).orElseThrow(() -> new EntityNotFoundException("COMPRA NAO LOCALIZADA"));
        ItemProduct itemProduct = new ItemProduct(product, purchase, itemProductDTO.quantity(), product.getPrice());
        ItemProduct itemProductSaved = itemProductRepository.save(itemProduct);
        return entityToResponse(itemProductSaved);

    }

    public ItemProductResponseDTO findItemProductById(Integer id) {
        return entityToResponse(itemProductRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ITEMPRODUCT NAO ENCONTRADO")));
    }


    public List<ItemProductResponseDTO> getAllItemProductByPurchaseId(Integer integer) {
        return itemProductRepository.findAllByPurchaseIdAndEnabledTrue(integer).stream().map(this::entityToResponse).toList();
    }

    public void deleteItemProduct(Integer id) {
        ItemProduct itemProduct = itemProductRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ITEMPRODUCT NAO ENCONTRADO"));
        itemProduct.setEnabled(false);
        itemProductRepository.save(itemProduct);

    }

    private ItemProductResponseDTO entityToResponse(ItemProduct item) {
        return new ItemProductResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getPurchase().getId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getCreateAt()
        );


    }

    public List<ItemProduct> createItemsForPurchase(List<ItemProductPurchaseRequestDTO> itemsDto, Purchase purchase) {
        return itemsDto.stream().map(dto -> {
            Product product = productRepository.findByIdAndEnabledTrue(dto.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            return new ItemProduct(product, purchase, dto.quantity(), product.getPrice());
        }).toList();
    }


}
