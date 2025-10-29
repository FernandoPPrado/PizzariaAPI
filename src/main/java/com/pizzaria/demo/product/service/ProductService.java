package com.pizzaria.demo.product.service;

import com.pizzaria.demo.product.dto.ProductRequestDTO;
import com.pizzaria.demo.product.dto.ProductResponseDTO;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Product product = productRepository.save(requestToEntity(productRequestDTO));
        return entityToResponse(product);
    }

    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO productRequestDTO) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        product.setProductName(productRequestDTO.productName());
        product.setDescription(productRequestDTO.description());
        product.setPrice(productRequestDTO.price());
        product.setActive(productRequestDTO.active());
        product.setCategory(productRequestDTO.category());

        productRepository.save(product);
        return entityToResponse(product);

    }

    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        productRepository.delete(product);
    }

    public void setActiveStatus(Integer id, boolean status) {

        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        product.setActive(status);
        productRepository.save(product);

    }

    public ProductResponseDTO getProductById(Integer id) {
        return entityToResponse(productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Produto não encontrado")));
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::entityToResponse).toList();
    }


    private Product requestToEntity(ProductRequestDTO prodReq) {
        return new Product(prodReq.productName(), prodReq.description(), prodReq.price(), prodReq.active(), prodReq.category());
    }

    private ProductResponseDTO entityToResponse(Product prodEnti) {
        return new ProductResponseDTO(prodEnti.getId(), prodEnti.getProductName(), prodEnti.getDescription(), prodEnti.getPrice(), prodEnti.isActive(), prodEnti.getCategory());
    }

}
