package com.pizzaria.demo.purchase.service;
import com.pizzaria.demo.itemProduct.model.ItemProduct;
import com.pizzaria.demo.itemProduct.service.ItemProductService;
import com.pizzaria.demo.purchase.dto.ItemProductPurchaseResponseDTO;
import com.pizzaria.demo.purchase.dto.PurchaseRequestDTO;
import com.pizzaria.demo.purchase.dto.PurchaseResponseDTO;
import com.pizzaria.demo.purchase.model.Purchase;
import com.pizzaria.demo.purchase.model.Status;
import com.pizzaria.demo.purchase.repository.PurchaseRepository;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class PurchaseService {


    private final ItemProductService itemProductService;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(ItemProductService itemProductService, UserRepository userRepository, PurchaseRepository purchaRepository) {

        this.itemProductService = itemProductService;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaRepository;
    }


    public PurchaseResponseDTO createPurchase(PurchaseRequestDTO purchaseRequestDTO, User owner) {
        log.info("Iniciando criação de purchase para userId = {}", owner.getId());


        try {

            Purchase purchase = new Purchase();

            purchase.setUser(userRepository.findByIdAndEnabledTrue(owner.getId()).orElseThrow(() -> {
                log.warn("Usuário não encontrado ou desativado ao criar purchase userId = {}", owner.getId());
                return new EntityNotFoundException("USUARIO NAO LOCALIZADO");
            }));

            log.debug("Criando itens da purchase para userId = {}", purchase.getUser().getId());

            List<ItemProduct> itemProductList = itemProductService.createItemsForPurchase(purchaseRequestDTO.itemProduct(), purchase);

            log.debug("Quantidade de itens na purchase = {}", itemProductList.size());

            purchase.setItems(itemProductList);

            log.debug("Calculando total da purchase para userId = {}", purchase.getUser().getId());
            purchase.calculateTotal();

            Purchase saved = purchaseRepository.save(purchase);

            log.info("Purchase criada com sucesso id = {} userId = {} total = {}",
                    saved.getId(), purchase.getUser().getId(), saved.getTotal());

            return entityToResponse(saved);

        } catch (Exception e) {
            log.error("Erro inesperado ao criar purchase para userId = {}", owner.getId(), e);
            throw e;
        }

    }

    public PurchaseResponseDTO getPurchaseById(Integer id) {
        log.debug("Buscando purchase no repositório id = {}", id);
        try {
            log.info("Iniciando busca de purchase por id = {}", id);
            Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Purchase não encontrada ou desativada id = {}", id);
                return new EntityNotFoundException("PURCHASE NAO ENCONTRADA");
            });
            log.info("Purchase localizada com sucesso id = {} total = {} status = {}",
                    purchase.getId(), purchase.getTotal(), purchase.getStatus());

            return entityToResponse(purchase);

        } catch (Exception e) {
            log.error("Erro inesperado ao buscar purchase id = {}", id, e);
            throw (e);
        }
    }

    public List<PurchaseResponseDTO> listPurchaseByUser(Integer userId) {

        log.info("Iniciando listagem de purchases do usuário userId = {}", userId);
        try {
            log.debug("Consultando repositório para buscar purchases enabled=true do userId = {}", userId);
            List<PurchaseResponseDTO> listPurchase = purchaseRepository.findAllByUser_IdAndEnabledTrue(userId).stream().map(this::entityToResponse).toList();
            log.info("Quantidade de purchases encontradas para userId = {}: {}", userId, listPurchase.size());
            return listPurchase;
        } catch (Exception e) {
            log.error("Erro inesperado ao listar purchases de userId = {}", userId, e);
            throw e;
        }
    }


    public Status updatePurchaseStatus(Integer purchaseId, Status status) {
        log.info("Iniciando atualização de status da purchase id = {} para {}", purchaseId, status);
        try {

            Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> {
                log.warn("Purchase não encontrada ou desativada ao tentar atualizar id = {}", purchaseId);
                return new EntityNotFoundException("PURCHASE NAO ENCONTRADA");
            });
            log.debug("Status antigo da purchase id = {} era {}", purchase.getId(), purchase.getStatus());
            purchase.setStatus(status);
            Purchase saved = purchaseRepository.save(purchase);
            log.info("Status da purchase id = {} atualizado com sucesso para {}", saved.getId(), saved.getStatus());
            return saved.getStatus();
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar status da purchase id = {}", purchaseId, e);
            throw (e);
        }
    }

    public void deletePurchase(Integer purchaseId) {
        log.info("Iniciando desativação (soft delete) da purchase id = {}", purchaseId);
        try {

            Purchase purchase = purchaseRepository.findByIdAndEnabledTrue(purchaseId).orElseThrow(() -> {
                log.warn("Purchase não encontrada ou já desativada id = {}", purchaseId);
                return new EntityNotFoundException("PURCHASE NAO ENCONTRADA");
            });


            log.debug("Dados da purchase antes da desativação id = {}\nStatus: {}\nTotal: {}\nEnabled: {}",
                    purchase.getId(),
                    purchase.getStatus(),
                    purchase.getTotal(),
                    purchase.isEnabled()
            );


            purchase.setEnabled(false);
            purchaseRepository.save(purchase);

            log.info("Purchase desativada com sucesso id = {}", purchaseId);

        } catch (Exception e) {

            log.error("Erro inesperado ao desativar purchase id = {}", purchaseId, e);
            throw e;
        }
    }


    private PurchaseResponseDTO entityToResponse(Purchase purchase) {
        return new PurchaseResponseDTO(purchase.getId(), purchase.getUser().getId(), purchase.getTotal(), purchase.getStatus(), purchase.getCreated(), purchase.getItems().stream().map(e -> new ItemProductPurchaseResponseDTO(e.getProduct().getId(), e.getProduct().getProductName(), e.getQuantity())).toList());
    }

}
