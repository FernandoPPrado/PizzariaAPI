package com.pizzaria.demo.product.service;

import com.pizzaria.demo.product.dto.ProductRequestDTO;
import com.pizzaria.demo.product.dto.ProductResponseDTO;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        log.info("Iniciando criação de produto com nome = {}", productRequestDTO.productName());
        try {
            log.debug("Mapeando DTO para entidade Product e salvando no banco, nome = {}", productRequestDTO.productName());
            Product product = productRepository.save(requestToEntity(productRequestDTO));
            log.info("Produto criado com sucesso id = {} nome = {}", product.getId(), product.getProductName());
            return entityToResponse(product);

        } catch (Exception e) {
            log.error("Erro inesperado ao criar produto com nome = {}", productRequestDTO.productName(), e);
            throw e;
        }


    }

    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO productRequestDTO) {

        log.info("Iniciando atualização de produto id = {}", id);

        try {

            Product product = productRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Produto não encontrado ou desativado ao tentar atualizar id = {}", id);
                return new EntityNotFoundException("Produto não encontrado");
            });


            log.debug("Dados antigos do produto id = {}\nNome: {}\nDescrição: {}\nPreço: {}\nAtivo: {}\nCategoria: {}\nImageUrl: {}", product.getId(), product.getProductName(), product.getDescription(), product.getPrice(), product.isActive(), product.getCategory(), product.getImageUrl());


            product.setProductName(productRequestDTO.productName());
            product.setDescription(productRequestDTO.description());
            product.setPrice(productRequestDTO.price());
            product.setActive(productRequestDTO.active());
            product.setCategory(productRequestDTO.category());
            product.setImageUrl(productRequestDTO.imageUrl());

            log.debug("Salvando produto atualizado id = {}", id);

            Product saved = productRepository.save(product);


            ProductResponseDTO resp = entityToResponse(saved);

            log.info("Produto atualizado com sucesso id = {}\nNovo Nome: {}\nNova Descrição: {}\nNovo Preço: {}\nNovo Ativo: {}\nNova Categoria: {}\nNova ImageUrl: {}", resp.id(), resp.productName(), resp.description(), resp.price(), resp.active(), resp.category(), resp.imageUrl());

            return resp;

        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar produto id = {}", id, e);
            throw e;
        }

    }

    public void deleteProduct(Integer id) {
        log.info("Iniciando desativação (soft delete) do produto id = {}", id);

        try {

            Product product = productRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Produto não encontrado ou já desativado id = {}", id);
                return new EntityNotFoundException("Produto não encontrado");
            });


            log.debug("Dados do produto antes da desativação id = {}\nNome: {}\nAtivo: {}\nEnabled: {}", product.getId(), product.getProductName(), product.isActive(), product.isEnabled());


            product.setEnabled(false);
            productRepository.save(product);
            log.info("Produto desativado com sucesso id = {} nome = {}", product.getId(), product.getProductName());

        } catch (Exception e) {
            log.error("Erro inesperado ao desativar produto id = {}", id, e);
            throw e;
        }


    }

    public void setActiveStatus(Integer id, boolean status) {
        log.info("Iniciando atualização de status 'active' do produto id = {} para {}", id, status);
        try {


            Product product = productRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Produto não encontrado ou desativado ao atualizar active id = {}", id);
                return new EntityNotFoundException("Produto não encontrado");
            });

            log.debug("Status antigo do produto id = {}: active = {}", product.getId(), product.isActive());

            product.setActive(status);
            productRepository.save(product);
            log.info("Status 'active' atualizado com sucesso id = {} novoActive = {}", product.getId(), product.isActive());


        } catch (Exception e) {
            log.error("Erro inesperado ao alterar status 'active' do produto id = {}", id, e);
            throw e;
        }

    }

    public ProductResponseDTO getProductById(Integer id) {

        log.info("Iniciando busca de produto por id = {}", id);
        try {
            log.debug("Buscando produto id = {} e mapeando para DTO", id);
            ProductResponseDTO prodDto = entityToResponse(productRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Produto não encontrado ou desativado id = {}", id);
                return new EntityNotFoundException("Produto não encontrado");
            }));
            log.info("Produto localizado com sucesso id = {} nome = {}", prodDto.id(), prodDto.productName());
            return prodDto;

        } catch (Exception e) {
            log.error("Erro inesperado ao buscar produto id = {}", id, e);
            throw e;
        }


    }

    public List<ProductResponseDTO> getAllProducts() {
        log.info("Iniciando listagem de produtos ativos");
        log.debug("Consultando repositório para buscar todos produtos enabled=true e mapeando para DTO");
        List<ProductResponseDTO> productList =
                productRepository.findAllByEnabledTrue().stream().map(this::entityToResponse).toList();
        log.info("Quantidade de produtos ativos encontrados = {}", productList.size());

        return productList;


    }


    private Product requestToEntity(ProductRequestDTO prodReq) {
        return new Product(prodReq.productName(), prodReq.description(), prodReq.price(), prodReq.active(), prodReq.category(), prodReq.imageUrl());
    }

    private ProductResponseDTO entityToResponse(Product prodEnti) {
        return new ProductResponseDTO(prodEnti.getId(), prodEnti.getProductName(), prodEnti.getDescription(), prodEnti.getPrice(), prodEnti.isActive(), prodEnti.getCategory(), prodEnti.getImageUrl());
    }


}
