package com.pizzaria.demo.purchase.repository;

import com.pizzaria.demo.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findAllByUser_IdAndEnabledTrue(Integer userId);

    Optional<Purchase> findByIdAndEnabledTrue(Integer id);
}
