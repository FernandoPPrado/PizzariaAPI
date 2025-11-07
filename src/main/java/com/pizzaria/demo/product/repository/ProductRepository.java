package com.pizzaria.demo.product.repository;

import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByProductNameAndEnabledTrue(String productName);

    Optional<Product> findByIdAndEnabledTrue(Integer id);

    List<Product> findAllByEnabledTrue();

    List<Product> findAllByIdAndEnabledTrue(List<Integer> itemsIdList);
}

