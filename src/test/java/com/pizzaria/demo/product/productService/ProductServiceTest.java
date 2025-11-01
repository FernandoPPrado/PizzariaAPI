package com.pizzaria.demo.product.productService;

import com.pizzaria.demo.product.repository.ProductRepository;
import com.pizzaria.demo.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;


    @Nested
    @DisplayName("Cenários Sucesso")
    class CenariosSucesso {



    }

    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {}

}
