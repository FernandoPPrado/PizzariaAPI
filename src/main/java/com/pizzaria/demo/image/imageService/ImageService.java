package com.pizzaria.demo.image.imageService;

import com.pizzaria.demo.product.model.Product;
import com.pizzaria.demo.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ImageService {

    private final ProductRepository productRepository;
    private final Path rootPath;

    public ImageService(ProductRepository productRepository, @Value("${pizzaria.images-dir}") String imagesDir) {
        this.productRepository = productRepository;
        this.rootPath = Paths.get(imagesDir).toAbsolutePath().normalize();

        try {

            Files.createDirectories(rootPath);
            log.info("Diretorio de imagens configurado em: {}", rootPath);

        } catch (Exception e) {
            log.error("Erro ao inicializar diretório de imagens em {}", rootPath, e);
            throw new IllegalStateException("Não foi possível criar/verificar diretório de imagens", e);
        }

    }

    public String saveProductImage(Integer productId, MultipartFile file) {
        log.info("Iniciando salvamento de imagem para productId={}", productId);

        if (file == null || file.isEmpty()) {
            log.warn("Tentativa de salvar imagem vazia para productId={}", productId);
            throw new IllegalArgumentException("Arquivo de imagem vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Tipo de arquivo inválido para productId={}, contentType={}", productId, contentType);
            throw new IllegalArgumentException("Tipo de arquivo inválido. Envie uma imagem.");
        }

        String extension = getExtensionFromContentType(contentType);
        String filename = buildFileName(productId, extension);

        Path target = rootPath.resolve(filename);
        log.debug("Salvando arquivo de imagem em {}", target);

        try {
            file.transferTo(target.toFile());
            String publicUrl = "/images/" + filename;
            log.info("Imagem salva com sucesso para productId={} url={}", productId, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Erro ao salvar imagem no disco para productId={} em {}", productId, target, e);
            throw new IllegalStateException("Erro ao salvar imagem do produto", e);
        }

    }


    public void deleteProductImage(Integer productId) {
        log.info("Iniciando remoção de imagem para productId={}", productId);

        Product product = productRepository.findByIdAndEnabledTrue(productId).orElseThrow(() -> {
            log.warn("Produto não encontrado ao tentar remover imagem, productId={}", productId);
            return new EntityNotFoundException("PRODUTO NAO ENCONTRADO");
        });

        if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
            log.info("Produto productId={} não possui imagem para remover", productId);
            return;
        }

        String fileName = Paths.get(product.getImageUrl()).getFileName().toString();
        Path target = rootPath.resolve(fileName);
        log.debug("Tentando deletar arquivo de imagem em {}", target);

        if (!Files.exists(target)) {
            log.warn("Arquivo de imagem não encontrado para productId={} em {}", productId, target);
        } else {
            try {
                Files.delete(target);
                log.info("Imagem removida do disco para productId={} path={}", productId, target);
            } catch (IOException e) {
                log.error("Erro ao tentar deletar imagem do disco para productId={} em {}", productId, target, e);
            }
        }
        product.setImageUrl(null);
        productRepository.save(product);
        log.info("Campo imageUrl limpo no produto productId={}", productId);


    }


    public String updateProductImage(Integer productId, MultipartFile newFile) {
        log.info("Iniciando atualização de imagem para productId={}", productId);

        if (newFile == null || newFile.isEmpty()) {
            log.warn("Tentativa de atualizar imagem com arquivo vazio para productId={}", productId);
            throw new IllegalArgumentException("Arquivo de imagem vazio");
        }

        deleteProductImage(productId);

        String newImageUrl = saveProductImage(productId, newFile);
        log.info("Nova imagem salva para productId={} url={}", productId, newImageUrl);


        Product product = productRepository.findByIdAndEnabledTrue(productId)
                .orElseThrow(() -> new EntityNotFoundException("PRODUTO NAO ENCONTRADO"));

        product.setImageUrl(newImageUrl);
        productRepository.save(product);

        return newImageUrl;
    }


    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> ".img"; // fallback simples
        };
    }

    private String buildFileName(Integer productId, String extension) {
        long timestamp = System.currentTimeMillis();
        return "produto-" + productId + "-" + timestamp + extension;
    }


}
