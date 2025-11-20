package com.pizzaria.demo.itemProduct.itemProductService;

import com.pizzaria.demo.itemProduct.dto.ItemProductRequestDTO;
import com.pizzaria.demo.itemProduct.dto.ItemProductResponseDTO;
import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.itemProduct.repository.ItemProductRepository;
import com.pizzaria.demo.itemProduct.service.ItemProductService;
import com.pizzaria.demo.product.model.Category;
import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseRequestDTO;
import com.pizzaria.demo.purchase.model.Purchase;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ItemProductServiceTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private ItemProductService itemProductService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemProductRepository itemProductRepository;


    @Nested
    @DisplayName("Cenários Sucesso")
    class CenariosSucesso {


        @Test
        public void createItemProductDeveRetornarSucesso() {
            //Arrange
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));
            ItemProductRequestDTO itemDto = new ItemProductRequestDTO(product.getId(), purchase.getId(), 3, new BigDecimal("1.2"));
            //Act
            ItemProductResponseDTO responseDTO = itemProductService.createItemProduct(itemDto);
            //Assert
            assertEquals(responseDTO.productId(), product.getId());
            assertEquals(responseDTO.purchaseId(), purchase.getId());
            assertEquals(responseDTO.quantity(), itemDto.quantity());

        }

        @Test
        public void findItemProductRetornaDTOCorreto() {
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));
            ;
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));
            ItemProduct saved = itemProductRepository.save(new ItemProduct(product, purchase, 2, new BigDecimal("1.2")));

            //Act
            ItemProductResponseDTO itemProductResponseDTO = itemProductService.findItemProductById(saved.getId());

            //Assert
            assertEquals(itemProductResponseDTO.productId(), saved.getProduct().getId());
            assertEquals(itemProductResponseDTO.purchaseId(), saved.getPurchase().getId());

        }

        @Test
        public void getAllItemPtoductByPurchaseIdRetornaListaCorreta() {
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));
            ItemProduct saved = itemProductRepository.save(new ItemProduct(product, purchase, 2, new BigDecimal("1.2")));
            ItemProduct saved2 = itemProductRepository.save(new ItemProduct(product, purchase, 3, new BigDecimal("1.3")));
            purchase.setItems(List.of(saved, saved2));

            List<ItemProductResponseDTO> itemProductList = itemProductService.getAllItemProductByPurchaseId(purchase.getId());

            assertEquals(2, itemProductList.size()); // dois itens
            assertTrue(itemProductList.stream()
                    .anyMatch(dto -> dto.productId().equals(product.getId()) && dto.quantity() == 2));
            assertTrue(itemProductList.stream()
                    .anyMatch(dto -> dto.productId().equals(product.getId()) && dto.quantity() == 3));

        }

        @Test
        public void deleteItemProductDeveDarSoftDeleteComSucesso() {

            //Arrange

            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));

            ItemProduct saved = itemProductRepository.save(new ItemProduct(product, purchase, 2, new BigDecimal("1.2")));

            //Act

            assertTrue(itemProductRepository.findById(saved.getId()).orElseThrow().isEnabled());
            itemProductService.deleteItemProduct(saved.getId());
            assertFalse(itemProductRepository.findById(saved.getId()).orElseThrow().isEnabled());

        }

        @Test
        public void createItemForPurchaseDeveCriarCorretamente() {
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));

            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));
            Product product2 = productRepository.save(new Product("Teste2", "TesteDesc2", new BigDecimal(2), true, Category.PIZZA));

            ItemProductPurchaseRequestDTO itemPurchaseDTO = new ItemProductPurchaseRequestDTO(product.getId(), 2);
            ItemProductPurchaseRequestDTO itemPurchaseDTO2 = new ItemProductPurchaseRequestDTO(product2.getId(), 1);

            //Act
            List<ItemProduct> itemProductList = itemProductService.createItemsForPurchase(List.of(itemPurchaseDTO, itemPurchaseDTO2), purchase);

            //Assert

            assertEquals(2, itemProductList.size());

            assertTrue(itemProductList.stream().anyMatch(e -> e.getProduct().getId().equals(itemPurchaseDTO.productId())));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getQuantity().equals(itemPurchaseDTO.quantity())));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getPurchase().equals(purchase)));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getProduct().getPrice().equals(product.getPrice())));

            assertTrue(itemProductList.stream().anyMatch(e -> e.getProduct().getId().equals(itemPurchaseDTO2.productId())));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getQuantity().equals(itemPurchaseDTO2.quantity())));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getPurchase().equals(purchase)));
            assertTrue(itemProductList.stream().anyMatch(e -> e.getProduct().getPrice().equals(product2.getPrice())));
        }


    }

    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {

        @Test
        public void createItemProductDeveLancarExcecao_QuandoProdutoNaoEncontrado() {

            //Arrange
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));
            ItemProductRequestDTO itemRequest = new ItemProductRequestDTO(-9999, purchase.getId(), 1, BigDecimal.TEN);

            //ActAssert
            assertThrows(EntityNotFoundException.class, () -> itemProductService.createItemProduct(itemRequest));

        }

        @Test
        public void findItemProductRetornaErroIdInexistente() {
            assertThrows(EntityNotFoundException.class, () -> itemProductService.findItemProductById(-99));
        }

        @Test
        public void deleteItemProductDeveErroItemNaoEncontrado() {

            assertThrows(EntityNotFoundException.class, () -> itemProductService.deleteItemProduct(-999));

        }

        @Test
        public void createItemForPurchaseDeveRetornarErroSeIdProdutoNaoExistirLista() {
            User user = userRepository.save(new User("UserTest", "EmailTest", "SenhaTeste", Role.ROLE_USER));
            Purchase purchase = purchaseRepository.save(new Purchase(user, new ArrayList<>()));

            Product product = productRepository.save(new Product("Teste", "TesteDesc", new BigDecimal(123.2), true, Category.PIZZA));

            ItemProductPurchaseRequestDTO itemPurchaseDTO = new ItemProductPurchaseRequestDTO(product.getId(), 2);
            ItemProductPurchaseRequestDTO itemPurchaseDTO2 = new ItemProductPurchaseRequestDTO(8, 1);

            //Act


            //Assert
            assertThrows(EntityNotFoundException.class, () -> itemProductService.createItemsForPurchase(List.of(itemPurchaseDTO, itemPurchaseDTO2), purchase));


        }
    }


}
