package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.dto.ProductRequest;
import com.gotcha.gotcha_api.model.dto.ProductResponse;
import com.gotcha.gotcha_api.repo.ProductRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    S3Service s3Service;

    @Autowired
    ProductRepo productRepo;

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepo.findAll();

        List<ProductResponse> productResponses = new ArrayList<>();

        for(Product product : products){
            if(product.getImageKey() != null){
                String signedUrl = s3Service.generateSignedUrl(product.getImageKey());
                if(signedUrl == null)
                    throw new ImageGenerationException("Image generation failed");
                product.setImageKey(signedUrl);
            }
            productResponses.add( new ProductResponse(product.getName(), product.getPrice(), product.getImageKey()));
        }

        return productResponses;
    }

    public void createProduct(@Valid ProductRequest productRequest, MultipartFile productImage) {

        Product newProduct = mapToProduct(productRequest);
        if (productImage != null && !productImage.isEmpty()) {
            try{
                String imageKey = s3Service.uploadFile(productImage, "product/");
                newProduct.setImageKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }
        }

        productRepo.save(newProduct);

    }
    private Product mapToProduct(ProductRequest productRequest){
        Product product = new Product();
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setBrand(productRequest.brand());
        product.setPrice(productRequest.price());
        product.setCategory(productRequest.category());
        product.setProductAvailable(productRequest.productAvailable());
        product.setReleaseDate(LocalDateTime.now());
        product.setStockQuantity(productRequest.stockQuantity());
        return product;
    }

    public void updateProduct(Long productId, @Valid ProductRequest productRequest, MultipartFile productImage) {
        Product product = getProductByID(productId);

        if (productImage != null && !productImage.isEmpty()) {
            try{
                String imageKey = s3Service.uploadFile(productImage, "product/");
                product.setImageKey(imageKey);
            }catch (IOException ex){
                throw new ImageFileNotFoundException("Image file not found", ex);
            }
        }

        productRepo.save(product);

    }

    public Product getProductByID(Long id) {
        return productRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Product", id)
        );
    }

    public void deleteProduct(Long productId) {
        Product product = getProductByID(productId);
        productRepo.delete(product);
    }
}
