package com.pizzaria.demo.itemProduct.repository;

import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemProductRepository extends JpaRepository<ItemProduct, Integer> {

    List<ItemProduct> findAllByPurchaseIdAndEnabledTrue(Integer purchaseId);

    Optional<ItemProduct> findByIdAndEnabledTrue(Integer purchaseId);


}
