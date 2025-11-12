package com.pizzaria.demo.purchase.service;

import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.itemProduct.repository.ItemProductRepository;
import com.pizzaria.demo.product.model.Category;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseResponseDTO;
import com.pizzaria.demo.purchase.model.Purchase;
import com.pizzaria.demo.purchase.model.Status;
import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
public class PurchaseServiceTest {


    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    ItemProductRepository itemProductRepository;
    @Autowired
    PurchaseRepository purchaseRepository;


    @Nested
    @DisplayName("Cenários Sucesso")
    class CenariosSucesso {

        @Test
        public void ceratePurchaseDeveCriarComUsuarioExistindoEListaDeProdutos() {

            //Arrange

            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));

            PurchaseRequestDTO purchaseRequestDTO = new PurchaseRequestDTO(user.getId(), List.of(new ItemProductPurchaseRequestDTO(product1.getId(), 3)));

            //act
            PurchaseResponseDTO purchaseResponseDTO = purchaseService.createPurchase(purchaseRequestDTO);

            //assert
            assertEquals(purchaseResponseDTO.userId(), user.getId());
            assertTrue(purchaseResponseDTO.items().stream().anyMatch(e -> e.productId().equals(product1.getId())));
            assertTrue(purchaseResponseDTO.items().stream().anyMatch(e -> e.productName().equals(product1.getProductName())));

        }

        @Test
        public void getPurchaseByIdDeveRetornarCompraCorretamente() {
            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));
            Purchase purchase = new Purchase();
            purchase.setUser(user);
            ItemProduct itemProduct = new ItemProduct(product1, purchase, 1, new BigDecimal("1.2"));
            purchase.setItems(List.of(itemProduct));
            purchaseRepository.save(purchase);


            //Act

            PurchaseResponseDTO purchaseResponseDTO = purchaseService.getPurchaseById(purchase.getId());

            //Assert
            assertEquals(purchaseResponseDTO.purchaseId(), purchase.getId());
            assertEquals(purchaseResponseDTO.userId(), user.getId());
            assertEquals(purchaseResponseDTO.status(), purchase.getStatus());
            assertTrue(purchaseResponseDTO.items().stream().anyMatch(e -> e.productId().equals(product1.getId())));
            assertEquals(purchaseResponseDTO.total(), purchase.getTotal());

        }

        @Test
        public void listPurchaseByUserDeveRetornarListaDeCompras() {

            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));
            Purchase purchase = new Purchase();
            purchase.setUser(user);
            ItemProduct itemProduct = new ItemProduct(product1, purchase, 1, new BigDecimal("1.2"));
            purchase.setItems(new ArrayList<>(List.of(itemProduct)));
            purchaseRepository.save(purchase);

            Purchase purchase2 = new Purchase();
            purchase2.setUser(user);
            purchase2.setItems(new ArrayList<>(List.of(itemProduct, itemProduct, itemProduct)));
            purchaseRepository.save(purchase2);

            //Act
            List<PurchaseResponseDTO> p = purchaseService.listPurchaseByUser(user.getId());
            assertEquals(2, p.size());

        }

        @Test
        public void listPurchaseByUserDeveRetornarListaVaziaDeCompras() {

            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));

            //Act
            List<PurchaseResponseDTO> p = purchaseService.listPurchaseByUser(user.getId());
            assertEquals(0, p.size());

        }

        @Test
        public void updatePurchaseStatusDeveAtualizarStatusOk() {

            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));

            PurchaseRequestDTO purchaseRequestDTO = new PurchaseRequestDTO(user.getId(), List.of(new ItemProductPurchaseRequestDTO(product1.getId(), 3)));

            //act
            PurchaseResponseDTO purchaseResponseDTO = purchaseService.createPurchase(purchaseRequestDTO);


            Status status = purchaseService.updatePurchaseStatus(purchaseResponseDTO.purchaseId(), Status.Teste);

            assertEquals(Status.Teste, status);


        }

        @Test
        public void deletePurchaseDeveDarSoftDelete() {

            User user = userRepository.save(new User("TESTE", "TESTE@GMAIL.COM", "TESTE", Role.ROLE_USER));
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));

            PurchaseRequestDTO purchaseRequestDTO = new PurchaseRequestDTO(user.getId(), List.of(new ItemProductPurchaseRequestDTO(product1.getId(), 3)));

            PurchaseResponseDTO purchaseResponseDTO = purchaseService.createPurchase(purchaseRequestDTO);


            //act
            purchaseService.deletePurchase(purchaseResponseDTO.purchaseId());

            //Assert
            assertFalse(purchaseRepository.findById(purchaseResponseDTO.purchaseId()).orElseThrow(() -> new EntityNotFoundException("Erro")).isEnabled());

        }

    }


    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {

        @Test
        public void createPurchaseDeveLancarExcessaoSeUsuarioNaoExistir() {
            Product product1 = productRepository.save(new Product("PROD1", "DESCRIP", new BigDecimal("1.2"), true, Category.PIZZA, "Link"));
            PurchaseRequestDTO purchaseRequestDTO = new PurchaseRequestDTO(1999, List.of(new ItemProductPurchaseRequestDTO(product1.getId(), 3)));

            //act
            assertThrows(EntityNotFoundException.class, () -> purchaseService.createPurchase(purchaseRequestDTO));


        }

        @Test
        public void getPurchaseDeveRetornarExcecaoSeCompaNaoExistir() {
            assertThrows(EntityNotFoundException.class, () -> purchaseService.getPurchaseById(99999));
        }

        @Test
        public void updatePurchaseStatusDeveRetornarErro() {

            assertThrows(EntityNotFoundException.class, () -> purchaseService.updatePurchaseStatus(9999, Status.Teste));


        }

        @Test
        public void deletePurchaseDeveRetornarErroSeNaoExistirPurchase() {

            assertThrows(EntityNotFoundException.class, () -> purchaseService.deletePurchase(9999));


        }

    }

}
