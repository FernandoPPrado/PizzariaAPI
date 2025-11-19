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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemProductService {

    @Autowired
    private ItemProductRepository itemProductRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;


    public ItemProductResponseDTO createItemProduct(ItemProductRequestDTO itemProductDTO) {

        log.info("Iniciando criação de ItemProduct para productId = {} purchaseId = {}", itemProductDTO.productId(), itemProductDTO.purchaseId());

        Product product = productRepository.findByIdAndEnabledTrue(itemProductDTO.productId()).orElseThrow(() -> {
            log.warn("Produto não encontrado ou desativado ao criar ItemProduct productId = {}", itemProductDTO.productId());
            return new EntityNotFoundException("PRODUTO NAO ENCONTRADO");
        });

        Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(itemProductDTO.purchaseId()).orElseThrow(() -> {
            log.warn("Purchase não encontrada ou desativada ao criar ItemProduct purchaseId = {}", itemProductDTO.purchaseId());
            return new EntityNotFoundException("COMPRA NAO LOCALIZADA");
        });

        ItemProduct itemProduct = new ItemProduct(product, purchase, itemProductDTO.quantity(), product.getPrice());

        ItemProduct itemProductSaved = itemProductRepository.save(itemProduct);

        log.info("ItemProduct criado com sucesso id = {} productId = {} purchaseId = {}", itemProductSaved.getId(), product.getId(), purchase.getId());

        return entityToResponse(itemProductSaved);

    }

    public ItemProductResponseDTO findItemProductById(Integer id) {
        log.info("Buscando ItemProduct por id = {}", id);
        return entityToResponse(itemProductRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
            log.warn("ItemProduct não encontrado ou desativado id = {}", id);
            return new EntityNotFoundException("ITEMPRODUCT NAO ENCONTRADO");
        }));
    }


    public List<ItemProductResponseDTO> getAllItemProductByPurchaseId(Integer purchaseId) {
        log.info("Listando ItemProducts da purchase id = {}", purchaseId);
        List<ItemProductResponseDTO> itemResponse = itemProductRepository.findAllByPurchaseIdAndEnabledTrue(purchaseId).stream().map(this::entityToResponse).toList();
        log.info("Quantidade de ItemProducts encontrados para purchase id = {}: {}", purchaseId, itemResponse.size());
        return itemResponse;

    }

    public void deleteItemProduct(Integer id) {
        log.info("Iniciando desativação de ItemProduct id = {}", id);
        ItemProduct itemProduct = itemProductRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
            log.warn("ItemProduct não encontrado ou já desativado id = {}", id);
            return new EntityNotFoundException("ITEMPRODUCT NAO ENCONTRADO");
        });
        itemProduct.setEnabled(false);
        itemProductRepository.save(itemProduct);
        log.info("ItemProduct desativado com sucesso id = {}", id);

    }


    public List<ItemProduct> createItemsForPurchase(List<ItemProductPurchaseRequestDTO> itemsDto, Purchase purchase) {

        log.debug("Criando itens para purchaseId = {} | quantidadeItens = {}",
                purchase.getId(), itemsDto.size());

        List<Integer> integerList = itemsDto.stream().map(ItemProductPurchaseRequestDTO::productId).toList();


        List<Product> productList = productRepository.findAllByIdInAndEnabledTrue(integerList);

        Map<Integer, Product> productMap = productList.stream().collect(Collectors.toMap(Product::getId, p -> p));

        return itemsDto.stream().map(dto -> {
            Product product = productMap.get(dto.productId());
            if (product == null) {
                log.warn("Produto não encontrado ao criar item para purchaseId = {} productId = {}",
                        purchase.getId(), dto.productId());
                throw new EntityNotFoundException("PRODUTO NAO ENCONTRADO");
            }

            return new ItemProduct(product, purchase, dto.quantity(), product.getPrice());
        }).collect(Collectors.toList());

    }

    private ItemProductResponseDTO entityToResponse(ItemProduct item) {
        return new ItemProductResponseDTO(item.getId(), item.getProduct().getId(), item.getProduct().getProductName(), item.getPurchase().getId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal(), item.getCreateAt());


    }


}
