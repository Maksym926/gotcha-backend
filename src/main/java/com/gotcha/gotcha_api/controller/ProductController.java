package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.Product;
import com.gotcha.gotcha_api.model.dto.ProductRequest;
import com.gotcha.gotcha_api.model.dto.ProductResponse;
import com.gotcha.gotcha_api.model.dto.ProductSearchParameter;
import com.gotcha.gotcha_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/member/product")
    public ResponseEntity<List<ProductResponse>> getAllProducts(@Valid @ModelAttribute ProductSearchParameter params) {
        return new ResponseEntity<>(productService.getAllProducts(params), HttpStatus.OK);
    }
    @GetMapping("/member/product/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId){
        return new ResponseEntity<>(productService.getProductByID(productId), HttpStatus.OK);
    }

    @PostMapping("/admin/product")
    public ResponseEntity<String> createProduct(@Valid @RequestPart("productRequest") ProductRequest productRequest, @RequestPart("productImage") MultipartFile productImage){
        productService.createProduct(productRequest, productImage);

        return new ResponseEntity<>("Product created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/admin/product/{productId}")
    public ResponseEntity<String> updateProduct(@PathVariable Long productId, @Valid @RequestPart("productRequest") ProductRequest productRequest, @RequestPart("productImage") MultipartFile productImage){
        productService.updateProduct(productId, productRequest, productImage);

        return new ResponseEntity<>("Product updated successfully", HttpStatus.OK);
    }
    @DeleteMapping("/admin/product/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);

        return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
    }


}
