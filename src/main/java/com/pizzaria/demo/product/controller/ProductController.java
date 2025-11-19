package com.pizzaria.demo.product.controller;

import com.pizzaria.demo.product.dto.ProductRequestDTO;
import com.pizzaria.demo.product.dto.ProductResponseDTO;
import com.pizzaria.demo.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductService productService;


    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}")
    public ResponseEntity<ProductResponseDTO> findByIdProduct(@PathVariable Integer id) {
        log.info("Recebida requisição para buscar produto por id = {}", id);

        ProductResponseDTO productResponseDTO = productService.getProductById(id);

        log.info("Produto retornado na requisição id = {} nome = {}", productResponseDTO.id(), productResponseDTO.productName());

        return ResponseEntity.ok(productResponseDTO);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    public ResponseEntity<List<ProductResponseDTO>> findAllProduct() {
        log.info("Recebida requisição para listar todos os produtos ativos");

        List<ProductResponseDTO> products = productService.getAllProducts();

        log.info("Quantidade de produtos retornados na requisição = {}", products.size());

        return ResponseEntity.ok(products);


    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody @Valid ProductRequestDTO productRequestDTO) {

        log.info("Recebida requisição para criar produto com nome = {}", productRequestDTO.productName());

        ProductResponseDTO responseDTO = productService.createProduct(productRequestDTO);
        log.info("Produto criado com sucesso na requisição id = {} nome = {}", responseDTO.id(), responseDTO.productName());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Integer id, @RequestBody @Valid ProductRequestDTO productRequestDTO) {

        log.info("Recebida requisição para atualizar produto id = {}", id);
        ProductResponseDTO responseDTO = productService.updateProduct(id, productRequestDTO);
        log.info("Produto atualizado com sucesso na requisição id = {} novoNome = {}", responseDTO.id(), responseDTO.productName());
        return ResponseEntity.ok(responseDTO);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {

        log.info("Recebida requisição para desativar (soft delete) produto id = {}", id);
        productService.deleteProduct(id);
        log.info("Produto desativado com sucesso na requisição id = {}", id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/status")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Integer id, @RequestParam boolean active) {


        log.info("Recebida requisição para atualizar status 'active' do produto id = {} para {}", id, active);
        productService.setActiveStatus(id, active);
        log.info("Status 'active' atualizado com sucesso na requisição id = {} novoActive = {}", id, active);
        return ResponseEntity.noContent().build();
    }


}
