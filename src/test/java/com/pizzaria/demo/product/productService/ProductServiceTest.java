package com.pizzaria.demo.product.productService;

import com.pizzaria.demo.product.dto.ProductRequestDTO;
import com.pizzaria.demo.product.dto.ProductResponseDTO;
import com.pizzaria.demo.product.model.Category;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import com.pizzaria.demo.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

        @Test
        public void criarProdutoDeveRetornarDtoQuandoProdutoCriado() {
            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productResponseDTO = productService.createProduct(productRequestDTO);
            assertEquals(productResponseDTO.description(), productRequestDTO.description());
            assertEquals(productResponseDTO.price(), productRequestDTO.price());
            assertEquals(productResponseDTO.active(), productRequestDTO.active());
            assertEquals(productResponseDTO.category(), productRequestDTO.category());
            assertEquals(productResponseDTO.imageUrl(), productRequestDTO.imageUrl());

        }

        @Test
        public void updateProductDeveRetornarDtoAtualizado() {

            ProductRequestDTO productToUpdate = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductRequestDTO productUpdate = new ProductRequestDTO("Atualizado", "Atualizado", new BigDecimal("20000.00"), false, Category.BEBIDA, "Testado.com");

            ProductResponseDTO productSaved = productService.createProduct(productToUpdate);
            ProductResponseDTO productUpdated = productService.updateProduct(productSaved.id(), productUpdate);

            assertNotEquals(productSaved.description(), productUpdated.description());
            assertNotEquals(productSaved.price(), productUpdated.price());
            assertNotEquals(productSaved.active(), productUpdated.active());
            assertNotEquals(productSaved.category(), productUpdated.category());
            assertNotEquals(productSaved.imageUrl(), productUpdated.imageUrl());

        }

        @Test
        public void deleteDeveMarcarEnabledComoFalso() {

            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);

            assertTrue(productRepository.findById(productSaved.id()).orElseThrow().isEnabled());
            productService.deleteProduct(productSaved.id());
            assertFalse(productRepository.findById(productSaved.id()).orElseThrow().isEnabled());

        }

        @Test
        public void serActiveDevePersistirCorretamente() {

            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);

            assertTrue(productRepository.findById(productSaved.id()).orElseThrow().isActive());
            productService.setActiveStatus(productSaved.id(), false);
            assertFalse(productRepository.findById(productSaved.id()).orElseThrow().isActive());

        }

        @Test
        public void getProductDeveRetornarDTOCorretoProduto() {

            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);

            ProductResponseDTO productFinded = productService.getProductById(productSaved.id());

            assertEquals(productSaved.description(), productFinded.description());
            assertEquals(productSaved.price(), productFinded.price());
            assertEquals(productSaved.active(), productFinded.active());
            assertEquals(productSaved.category(), productFinded.category());
            assertEquals(productSaved.imageUrl(), productFinded.imageUrl());

        }

        @Test
        public void getAllProductDeveRetornarDTOCorretoProduto() {

            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductRequestDTO productRequestDTO2 = new ProductRequestDTO("Teste2", "Teste2", new BigDecimal("1.220"), true, Category.PIZZA, "youtube2.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);
            ProductResponseDTO productSaved2 = productService.createProduct(productRequestDTO2);
            productService.deleteProduct(productSaved2.id());

            List<ProductResponseDTO> productFinded = productService.getAllProducts();

            assertTrue(productFinded.stream().allMatch(p -> {
                Product entity = productRepository.findById(p.id()).orElseThrow();
                return entity.isEnabled();
            }));


        }

    }

    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {


        @Test
        public void updateProductDeveRetornarErroSeNaoExistir() {
            ProductRequestDTO productToUpdate = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductRequestDTO productUpdate = new ProductRequestDTO("Atualizado", "Atualizado", new BigDecimal("20000.00"), false, Category.BEBIDA, "Testado.com");
            ProductResponseDTO productSaved = productService.createProduct(productToUpdate);
            assertThrows(EntityNotFoundException.class,
                    () -> productService.updateProduct(productSaved.id() * -1, productUpdate));

        }

        @Test
        public void deleteProductDeveRetornarErroSeNaoExistir() {
            assertThrows(EntityNotFoundException.class,
                    () -> productService.deleteProduct(-1));

        }


        @Test
        public void serActiveNaoDevePersistirCorretamenteSeDeletado() {
            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);
            assertTrue(productRepository.findById(productSaved.id()).orElseThrow().isActive());
            productService.deleteProduct(productSaved.id());
            assertThrows(EntityNotFoundException.class, () -> productService.setActiveStatus(productSaved.id(), false));
        }

        @Test
        public void getProductDeveRetornarErroSeProdutoNaoExistir() {

            ProductRequestDTO productRequestDTO = new ProductRequestDTO("Teste", "Teste", new BigDecimal("1.20"), true, Category.PIZZA, "youtube.com");
            ProductResponseDTO productSaved = productService.createProduct(productRequestDTO);
            productService.deleteProduct(productSaved.id());
            assertThrows(EntityNotFoundException.class, () -> productService.getProductById(productSaved.id()));
        }


    }
}
