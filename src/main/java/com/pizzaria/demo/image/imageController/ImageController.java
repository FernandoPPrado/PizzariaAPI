package com.pizzaria.demo.image.imageController;
import com.pizzaria.demo.image.dto.ImageUploadResponseDTO;
import com.pizzaria.demo.image.imageService.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(path = "/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/product/{prodId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDTO> postImage(@PathVariable Integer prodId, @RequestParam("file") MultipartFile file) {
        log.info("Recebida requisicao de upload de imagem");
        String imageUrl = imageService.saveProductImage(prodId, file);
        log.info("Imagem salva para o produto = {}", prodId);
        return ResponseEntity.ok(new ImageUploadResponseDTO(prodId, imageUrl));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/product/{prodId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer prodId) {
        log.info("Recebida requisição para deletar imagem do produto={}", prodId);
        imageService.deleteProductImage(prodId);
        log.info("Imagem deletada para o produto = {}", prodId);
        return ResponseEntity.noContent().build();

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/product/{prodId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponseDTO> updateImage(@PathVariable Integer prodId, @RequestParam("file") MultipartFile file) {

        log.info("Recebida requisicao de update de imagem");
        String imageUrl = imageService.updateProductImage(prodId, file);
        log.info("Imagem atualizada para o produto = {}", prodId);
        return ResponseEntity.ok(new ImageUploadResponseDTO(prodId, imageUrl));

    }
}
