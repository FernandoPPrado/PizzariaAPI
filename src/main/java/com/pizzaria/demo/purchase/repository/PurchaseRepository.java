package com.pizzaria.demo.purchase.repository;

import com.pizzaria.demo.purchase.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
}
