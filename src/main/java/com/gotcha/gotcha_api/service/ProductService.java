package com.gotcha.gotcha_api.service;

import com.gotcha.gotcha_api.exception.custom.ImageFileNotFoundException;
import com.gotcha.gotcha_api.exception.custom.ImageGenerationException;
import com.gotcha.gotcha_api.exception.custom.ResourceNotFoundException;
import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.dto.ProductRequest;
import com.gotcha.gotcha_api.model.dto.ProductResponse;
import com.gotcha.gotcha_api.model.dto.ProductSearchParameter;
import com.gotcha.gotcha_api.repo.ProductRepo;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    public List<ProductResponse> getAllProducts(ProductSearchParameter searchParams) {
        Specification<Product> specification = search(searchParams);
        List<Product> products = productRepo.findAll(specification);

        List<ProductResponse> productResponses = new ArrayList<>();

        for(Product product : products){
            String signedUrl = "";
            if(product.getImageKey() != null){
                signedUrl = s3Service.generateSignedUrl(product.getImageKey());
                if(signedUrl.isBlank())
                    throw new ImageGenerationException("Image generation failed");
                product.setImageKey(signedUrl);
            }
            productResponses.add( new ProductResponse(product.getName(), product.getPrice(), signedUrl));
        }

        return productResponses;
    }

    public static Specification<Product> search( ProductSearchParameter params) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(StringUtils.hasText(params.keyword())){
                String pattern = "%" + params.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if(StringUtils.hasText(params.brand())){
                predicates.add(cb.equal(root.get("brand"), params.brand()));
            }
            if(StringUtils.hasText(params.category())){
                predicates.add(cb.equal(root.get("category"), params.category()));
            }
            if(params.minPrice() != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), params.minPrice()));
            }
            if(params.maxPrice() != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), params.maxPrice()));
            }
            if(params.productAvailable() != null){
                predicates.add(cb.equal(root.get("productAvailable"), params.productAvailable()));
            }

            return  cb.and(predicates.toArray(new Predicate[0]));
        };
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
